package com.wolfe.retailer;

import com.wolfe.catalog.Product;
import com.wolfe.catalog.ProductRepository;
import com.wolfe.catalog.ProductVariant;
import com.wolfe.catalog.ProductVariantRepository;
import com.wolfe.order.Order;
import com.wolfe.order.OrderItem;
import com.wolfe.order.OrderItemRepository;
import com.wolfe.order.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

@Service
public class RetailerAllocationService {
    private final RetailerRepository retailerRepo;
    private final RetailerServiceAreaRepository serviceAreaRepo;
    private final RetailerInventoryRepository inventoryRepo;
    private final RetailerInventoryService inventoryService;
    private final RetailerOrderAssignmentRepository assignmentRepo;
    private final FulfillmentRepository fulfillmentRepo;
    private final RetailerSettlementService settlementService;
    private final RetailerAuditLogRepository auditRepo;
    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final ProductRepository productRepo;
    private final ProductVariantRepository variantRepo;

    public RetailerAllocationService(RetailerRepository retailerRepo,
                                   RetailerServiceAreaRepository serviceAreaRepo,
                                   RetailerInventoryRepository inventoryRepo,
                                   RetailerInventoryService inventoryService,
                                   RetailerOrderAssignmentRepository assignmentRepo,
                                   FulfillmentRepository fulfillmentRepo,
                                   RetailerSettlementService settlementService,
                                   RetailerAuditLogRepository auditRepo,
                                   OrderRepository orderRepo,
                                   OrderItemRepository orderItemRepo,
                                   ProductRepository productRepo,
                                   ProductVariantRepository variantRepo) {
        this.retailerRepo = retailerRepo;
        this.serviceAreaRepo = serviceAreaRepo;
        this.inventoryRepo = inventoryRepo;
        this.inventoryService = inventoryService;
        this.assignmentRepo = assignmentRepo;
        this.fulfillmentRepo = fulfillmentRepo;
        this.settlementService = settlementService;
        this.auditRepo = auditRepo;
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.productRepo = productRepo;
        this.variantRepo = variantRepo;
    }

    public record AllocationCandidate(
            Retailer retailer,
            boolean serviceable,
            boolean hasAllStock,
            int matchingItemsCount,
            int totalItemsCount,
            int etaHours
    ) {}

    public List<AllocationCandidate> evaluateEligibleRetailers(String orderId) {
        Order order = orderRepo.findById(orderId).orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId));
        List<OrderItem> items = orderItemRepo.findByOrderId(orderId);
        List<Retailer> allActive = retailerRepo.findByStatusAndVerificationStatus("ACTIVE", "VERIFIED");

        List<AllocationCandidate> candidates = new ArrayList<>();

        for (Retailer r : allActive) {
            List<RetailerServiceArea> areas = serviceAreaRepo.findByRetailerId(r.getId());
            boolean serviceable = areas.stream().anyMatch(a -> a.isActive() && (
                    a.getPincode().trim().equalsIgnoreCase(order.getPincode().trim()) ||
                    a.getCity().trim().equalsIgnoreCase(order.getCity().trim())
            ));

            int eta = areas.stream()
                    .filter(a -> a.isActive() && a.getPincode().trim().equalsIgnoreCase(order.getPincode().trim()))
                    .mapToInt(RetailerServiceArea::getDeliveryEtaHours)
                    .min()
                    .orElse(24);

            int matchingItems = 0;
            boolean hasAllStock = true;

            for (OrderItem item : items) {
                String sku = item.getVariantSku();
                if (sku == null || sku.isBlank()) {
                    Product p = productRepo.findById(item.getProductId()).orElse(null);
                    sku = p != null ? "SKU-" + p.getSlug().toUpperCase() : "SKU-" + item.getProductId();
                }

                Optional<RetailerInventory> inv = inventoryRepo.findByRetailerIdAndSku(r.getId(), sku);
                if (inv.isPresent() && inv.get().getAvailableStock() >= item.getQuantity()) {
                    matchingItems++;
                } else {
                    hasAllStock = false;
                }
            }

            candidates.add(new AllocationCandidate(r, serviceable, hasAllStock, matchingItems, items.size(), eta));
        }

        candidates.sort((a, b) -> {
            if (a.hasAllStock() != b.hasAllStock()) return Boolean.compare(b.hasAllStock(), a.hasAllStock());
            if (a.serviceable() != b.serviceable()) return Boolean.compare(b.serviceable(), a.serviceable());
            if (a.etaHours() != b.etaHours()) return Integer.compare(a.etaHours(), b.etaHours());
            return b.retailer().getRating().compareTo(a.retailer().getRating());
        });

        return candidates;
    }

    @Transactional
    public boolean allocateOrder(Order order, List<OrderItem> items) {
        List<AllocationCandidate> candidates = evaluateEligibleRetailers(order.getId());
        AllocationCandidate best = candidates.stream()
                .filter(c -> c.serviceable() && c.hasAllStock())
                .findFirst()
                .orElse(null);

        if (best == null) {
            auditRepo.save(new RetailerAuditLog(
                    "OrderAllocation", order.getId(),
                    "AUTO_ALLOCATE_UNASSIGNED", "SYSTEM",
                    "No single eligible retailer has full stock in service area for pincode: " + order.getPincode()
            ));
            return false;
        }

        assignToRetailer(order.getId(), best.retailer().getId(), "AUTO_ALLOCATION", "Auto-allocated to nearest stocked partner: " + best.retailer().getName());
        return true;
    }

    @Transactional
    public void assignToRetailer(String orderId, Long retailerId, String assignedBy, String notes) {
        Order order = orderRepo.findById(orderId).orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId));
        Retailer retailer = retailerRepo.findById(retailerId).orElseThrow(() -> new NoSuchElementException("Retailer not found: " + retailerId));
        List<OrderItem> items = orderItemRepo.findByOrderId(orderId);

        // 1. If previously assigned to another retailer, release previous reservations
        Optional<RetailerOrderAssignment> prevAssignment = assignmentRepo.findTopByOrderIdOrderByIdDesc(orderId);
        if (prevAssignment.isPresent() && !prevAssignment.get().getRetailerId().equals(retailerId) &&
                !"REJECTED".equals(prevAssignment.get().getStatus()) && !"CANCELLED".equals(prevAssignment.get().getStatus())) {
            Long prevRetId = prevAssignment.get().getRetailerId();
            for (OrderItem it : items) {
                String sku = resolveSku(it);
                inventoryService.releaseStock(prevRetId, it.getProductId(), it.getVariantId(), sku, it.getQuantity(), orderId, assignedBy);
            }
            prevAssignment.get().setStatus("REASSIGNED");
            assignmentRepo.save(prevAssignment.get());
        }

        // 2. Reserve stock at new retailer
        for (OrderItem it : items) {
            String sku = resolveSku(it);
            inventoryService.reserveStock(retailerId, it.getProductId(), it.getVariantId(), sku, it.getQuantity(), orderId, assignedBy);
        }

        // 3. Create or update RetailerOrderAssignment
        RetailerOrderAssignment assignment = new RetailerOrderAssignment(orderId, retailerId, assignedBy, notes);
        assignmentRepo.save(assignment);

        // 4. Create or update Fulfillment record
        Fulfillment fulfillment = fulfillmentRepo.findByOrderId(orderId)
                .orElse(new Fulfillment(orderId, retailerId));
        fulfillment.setStatus("ASSIGNED");
        fulfillment.setCourierName("Wolfe Local Express");
        fulfillmentRepo.save(fulfillment);

        // 5. Initialize Settlement
        long totalGross = 0;
        long totalWolfeMargin = 0;
        long totalRetailerPayout = 0;

        for (OrderItem it : items) {
            Product p = productRepo.findById(it.getProductId()).orElse(null);
            String cat = p != null ? p.getCategory() : "Hardware";
            long lineGross = it.getUnitPrice() * it.getQuantity();
            var calc = settlementService.calculateLineItemSettlement(retailerId, cat, it.getProductId(), it.getVariantId(), lineGross);

            totalGross += calc.grossAmount();
            totalWolfeMargin += calc.wolfeMarginAmount();
            totalRetailerPayout += calc.retailerPayableAmount();
        }

        settlementService.initializeSettlement(orderId, retailerId, totalGross, totalWolfeMargin, totalRetailerPayout);

        auditRepo.save(new RetailerAuditLog(
                "OrderAllocation", orderId,
                "ASSIGNED", assignedBy,
                "Order assigned to retailer #" + retailerId + " (" + retailer.getName() + "). Stock reserved."
        ));
    }

    @Transactional
    public void acceptOrder(String orderId, Long retailerId) {
        RetailerOrderAssignment assignment = assignmentRepo.findByOrderIdAndRetailerId(orderId, retailerId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found for order: " + orderId + " and retailer: " + retailerId));
        if ("CANCELLED".equalsIgnoreCase(assignment.getStatus()) || "REJECTED".equalsIgnoreCase(assignment.getStatus())) {
            throw new IllegalStateException("Cannot accept order in status: " + assignment.getStatus());
        }
        assignment.accept();
        assignmentRepo.save(assignment);

        Fulfillment fulfillment = fulfillmentRepo.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Fulfillment record not found for order: " + orderId));
        if ("DELIVERED".equalsIgnoreCase(fulfillment.getStatus()) || "CANCELLED".equalsIgnoreCase(fulfillment.getStatus())) {
            throw new IllegalStateException("Cannot accept order when fulfillment is: " + fulfillment.getStatus());
        }
        fulfillment.setStatus("ACCEPTED");
        fulfillmentRepo.save(fulfillment);

        Order order = orderRepo.findById(orderId).orElseThrow();
        if ("CONFIRMED".equalsIgnoreCase(order.getStatus())) {
            order.setStatus("PROCESSING");
            orderRepo.save(order);
        }

        auditRepo.save(new RetailerAuditLog(
                "Fulfillment", orderId, "ACCEPTED", "RETAILER_" + retailerId, "Order accepted by retailer #" + retailerId
        ));
    }

    @Transactional
    public void packOrder(String orderId, Long retailerId, String trackingNumber, String courierName) {
        Fulfillment fulfillment = fulfillmentRepo.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Fulfillment record not found for order: " + orderId));
        if (!fulfillment.getRetailerId().equals(retailerId)) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized fulfillment update");
        }
        if (!"ACCEPTED".equalsIgnoreCase(fulfillment.getStatus()) && !"PACKED".equalsIgnoreCase(fulfillment.getStatus())) {
            throw new IllegalStateException("Cannot pack order with fulfillment status: " + fulfillment.getStatus());
        }

        fulfillment.setStatus("PACKED");
        fulfillment.setPackedAt(OffsetDateTime.now());
        if (trackingNumber != null && !trackingNumber.isBlank()) fulfillment.setTrackingNumber(trackingNumber.trim());
        if (courierName != null && !courierName.isBlank()) fulfillment.setCourierName(courierName.trim());
        fulfillmentRepo.save(fulfillment);

        Order order = orderRepo.findById(orderId).orElseThrow();
        if (!"SHIPPED".equalsIgnoreCase(order.getStatus()) && !"DELIVERED".equalsIgnoreCase(order.getStatus())) {
            order.setStatus("PROCESSING");
            orderRepo.save(order);
        }

        auditRepo.save(new RetailerAuditLog(
                "Fulfillment", orderId, "PACKED", "RETAILER_" + retailerId, "Order packed. Tracking: " + trackingNumber
        ));
    }

    @Transactional
    public void markReady(String orderId, Long retailerId) {
        Fulfillment fulfillment = fulfillmentRepo.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Fulfillment record not found"));
        if (!fulfillment.getRetailerId().equals(retailerId)) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized fulfillment update");
        }
        if (!"PACKED".equalsIgnoreCase(fulfillment.getStatus()) && !"READY_FOR_DELIVERY".equalsIgnoreCase(fulfillment.getStatus())) {
            throw new IllegalStateException("Cannot mark ready from status: " + fulfillment.getStatus());
        }

        fulfillment.setStatus("READY_FOR_DELIVERY");
        fulfillmentRepo.save(fulfillment);

        auditRepo.save(new RetailerAuditLog(
                "Fulfillment", orderId, "READY_FOR_DELIVERY", "RETAILER_" + retailerId, "Order ready for local delivery"
        ));
    }

    @Transactional
    public void outForDelivery(String orderId, Long retailerId, String trackingNumber) {
        Fulfillment fulfillment = fulfillmentRepo.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Fulfillment record not found"));
        if (!fulfillment.getRetailerId().equals(retailerId)) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized fulfillment update");
        }
        if (!"READY_FOR_DELIVERY".equalsIgnoreCase(fulfillment.getStatus()) && !"PACKED".equalsIgnoreCase(fulfillment.getStatus()) && !"OUT_FOR_DELIVERY".equalsIgnoreCase(fulfillment.getStatus())) {
            throw new IllegalStateException("Cannot dispatch order from status: " + fulfillment.getStatus());
        }

        fulfillment.setStatus("OUT_FOR_DELIVERY");
        fulfillment.setShippedAt(OffsetDateTime.now());
        if (trackingNumber != null && !trackingNumber.isBlank()) fulfillment.setTrackingNumber(trackingNumber.trim());
        fulfillmentRepo.save(fulfillment);

        Order order = orderRepo.findById(orderId).orElseThrow();
        order.setStatus("SHIPPED");
        orderRepo.save(order);

        auditRepo.save(new RetailerAuditLog(
                "Fulfillment", orderId, "OUT_FOR_DELIVERY", "RETAILER_" + retailerId, "Order dispatched out for delivery"
        ));
    }

    @Transactional
    public void deliverOrder(String orderId, Long retailerId) {
        Fulfillment fulfillment = fulfillmentRepo.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Fulfillment record not found"));
        if (!fulfillment.getRetailerId().equals(retailerId)) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized fulfillment update");
        }
        if (!"OUT_FOR_DELIVERY".equalsIgnoreCase(fulfillment.getStatus()) && !"READY_FOR_DELIVERY".equalsIgnoreCase(fulfillment.getStatus()) && !"PACKED".equalsIgnoreCase(fulfillment.getStatus())) {
            throw new IllegalStateException("Cannot deliver order from status: " + fulfillment.getStatus());
        }

        fulfillment.setStatus("DELIVERED");
        fulfillment.setDeliveredAt(OffsetDateTime.now());
        fulfillmentRepo.save(fulfillment);

        // 1. Fulfill inventory (deduct physical and release reservation)
        List<OrderItem> items = orderItemRepo.findByOrderId(orderId);
        for (OrderItem it : items) {
            String sku = resolveSku(it);
            inventoryService.fulfillStock(retailerId, it.getProductId(), it.getVariantId(), sku, it.getQuantity(), orderId, "RETAILER_" + retailerId);
        }

        // 2. Mark settlement eligible
        settlementService.markEligible(orderId);

        // 3. Mark Wolfe order delivered
        Order order = orderRepo.findById(orderId).orElseThrow();
        order.setStatus("DELIVERED");
        orderRepo.save(order);

        auditRepo.save(new RetailerAuditLog(
                "Fulfillment", orderId, "DELIVERED", "RETAILER_" + retailerId, "Order delivered to customer and inventory finalized."
        ));
    }

    @Transactional
    public void rejectOrder(String orderId, Long retailerId, String reason) {
        RetailerOrderAssignment assignment = assignmentRepo.findByOrderIdAndRetailerId(orderId, retailerId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
        if ("DELIVERED".equalsIgnoreCase(assignment.getStatus()) || "CANCELLED".equalsIgnoreCase(assignment.getStatus())) {
            throw new IllegalStateException("Cannot reject order in status: " + assignment.getStatus());
        }
        assignment.reject(reason);
        assignmentRepo.save(assignment);

        List<OrderItem> items = orderItemRepo.findByOrderId(orderId);
        for (OrderItem it : items) {
            String sku = resolveSku(it);
            inventoryService.releaseStock(retailerId, it.getProductId(), it.getVariantId(), sku, it.getQuantity(), orderId, "RETAILER_" + retailerId);
        }

        Fulfillment fulfillment = fulfillmentRepo.findByOrderId(orderId).orElse(null);
        if (fulfillment != null) {
            fulfillment.setStatus("FAILED_DELIVERY");
            fulfillment.setFailedReason(reason);
            fulfillment.setFailedAt(OffsetDateTime.now());
            fulfillmentRepo.save(fulfillment);
        }

        auditRepo.save(new RetailerAuditLog(
                "OrderAllocation", orderId, "REJECTED", "RETAILER_" + retailerId, "Order rejected by retailer. Reason: " + reason
        ));
    }

    @Transactional
    public void cancelAllocation(String orderId) {
        Optional<RetailerOrderAssignment> assignment = assignmentRepo.findTopByOrderIdOrderByIdDesc(orderId);
        if (assignment.isPresent() && !"CANCELLED".equals(assignment.get().getStatus())) {
            Long retailerId = assignment.get().getRetailerId();
            List<OrderItem> items = orderItemRepo.findByOrderId(orderId);
            for (OrderItem it : items) {
                String sku = resolveSku(it);
                try {
                    inventoryService.releaseStock(retailerId, it.getProductId(), it.getVariantId(), sku, it.getQuantity(), orderId, "ORDER_CANCELLED");
                } catch (Exception ignored) {}
            }
            assignment.get().setStatus("CANCELLED");
            assignmentRepo.save(assignment.get());
        }

        fulfillmentRepo.findByOrderId(orderId).ifPresent(f -> {
            f.setStatus("CANCELLED");
            fulfillmentRepo.save(f);
        });
    }

    private String resolveSku(OrderItem it) {
        if (it.getVariantSku() != null && !it.getVariantSku().isBlank()) {
            return it.getVariantSku().trim();
        }
        Product p = productRepo.findById(it.getProductId()).orElse(null);
        return p != null ? "SKU-" + p.getSlug().toUpperCase() : "SKU-" + it.getProductId();
    }
}

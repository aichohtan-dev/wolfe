package com.wolfe.retailer;

import com.wolfe.catalog.Product;
import com.wolfe.catalog.ProductRepository;
import com.wolfe.catalog.ProductVariant;
import com.wolfe.catalog.ProductVariantRepository;
import com.wolfe.inventory.Inventory;
import com.wolfe.inventory.InventoryRepository;
import com.wolfe.order.Order;
import com.wolfe.order.OrderItem;
import com.wolfe.order.OrderItemRepository;
import com.wolfe.order.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.NoSuchElementException;
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
    private final InventoryRepository globalInventoryRepo;
    private final RetailerAuditWriter auditWriter;

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
                                   ProductVariantRepository variantRepo,
                                   InventoryRepository globalInventoryRepo,
                                   RetailerAuditWriter auditWriter) {
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
        this.globalInventoryRepo = globalInventoryRepo;
        this.auditWriter = auditWriter;
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
        List<RetailerServiceArea> pincodeAreas = serviceAreaRepo.findByPincodeAndActiveTrue(order.getPincode());

        List<AllocationCandidate> candidates = new ArrayList<>();

        for (Retailer r : allActive) {
            List<RetailerServiceArea> areas = pincodeAreas.stream().filter(a -> java.util.Objects.equals(a.getRetailerId(), r.getId())).toList();
            boolean areaServiceable = areas.stream().anyMatch(a -> a.isActive() && (
                    a.getPincode().trim().equalsIgnoreCase(order.getPincode().trim()) ||
                    a.getCity().trim().equalsIgnoreCase(order.getCity().trim())
            ));
            boolean radiusServiceable = false;
            if (order.getLatitude() != null && order.getLongitude() != null && r.getLatitude() != null && r.getLongitude() != null && r.getDeliveryRadiusKm() != null) {
                radiusServiceable = haversineKm(order.getLatitude().doubleValue(), order.getLongitude().doubleValue(),
                        r.getLatitude().doubleValue(), r.getLongitude().doubleValue()) <= r.getDeliveryRadiusKm().doubleValue();
            }
            boolean serviceable = radiusServiceable || areaServiceable;

            int eta = areas.stream()
                    .filter(a -> a.isActive() && a.getPincode().trim().equalsIgnoreCase(order.getPincode().trim()))
                    .mapToInt(RetailerServiceArea::getDeliveryEtaHours)
                    .min()
                    .orElse(24);

            int matchingItems = 0;
            boolean hasAllStock = true;
            Map<String, Integer> requiredBySku = new LinkedHashMap<>();
            for (OrderItem item : items) requiredBySku.merge(resolveSku(item), item.getQuantity(), Integer::sum);
            Map<String, RetailerInventory> stockBySku = inventoryRepo
                    .findByRetailerIdAndSkuIn(r.getId(), requiredBySku.keySet())
                    .stream().collect(java.util.stream.Collectors.toMap(RetailerInventory::getSku, x -> x));
            for (var req : requiredBySku.entrySet()) {
                RetailerInventory inv = stockBySku.get(req.getKey());
                if (inv != null && inv.getAvailableStock() >= req.getValue()) matchingItems++;
                else hasAllStock = false;
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

        try {
            assignToRetailer(order.getId(), best.retailer().getId(), "AUTO_ALLOCATION", "Auto-allocated to nearest stocked partner: " + best.retailer().getName());
            return true;
        } catch (RuntimeException ex) {
            auditWriter.allocationFailure(order.getId(), ex);
            throw ex;
        }
    }

    @Transactional
    public void assignToRetailer(String orderId, Long retailerId, String assignedBy, String notes) {
        Order order = orderRepo.findByIdForUpdate(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId));
        if (Set.of("CANCELLED", "DELIVERED").contains(order.getStatus().toUpperCase())) {
            throw new IllegalStateException("Cannot assign retailer for order in status: " + order.getStatus());
        }

        Retailer retailer = retailerRepo.findById(retailerId)
                .orElseThrow(() -> new NoSuchElementException("Retailer not found: " + retailerId));
        if (!"ACTIVE".equalsIgnoreCase(retailer.getStatus()) || !"VERIFIED".equalsIgnoreCase(retailer.getVerificationStatus())) {
            throw new IllegalStateException("Retailer is not active and verified");
        }

        List<OrderItem> items = orderItemRepo.findByOrderId(orderId);
        Optional<RetailerOrderAssignment> prevAssignment = assignmentRepo.findTopByOrderIdOrderByIdDesc(orderId);

        // Idempotent re-submit: never reserve the same retailer's stock twice.
        if (prevAssignment.isPresent()
                && retailerId.equals(prevAssignment.get().getRetailerId())
                && !Set.of("REJECTED", "CANCELLED", "REASSIGNED", "EXPIRED").contains(prevAssignment.get().getStatus())) {
            return;
        }

        // Validate the current fulfillment before mutating inventory or assignment history.
        // Reassignment is append-only: historical fulfillment rows are never retargeted to another retailer.
        Optional<Fulfillment> currentFulfillment = fulfillmentRepo.findByOrderIdForUpdate(orderId);
        if (currentFulfillment.isPresent()
                && !Set.of("ASSIGNED", "FAILED_DELIVERY", "CANCELLED", "REASSIGNED").contains(currentFulfillment.get().getStatus().toUpperCase())) {
            throw new IllegalStateException("Cannot reassign order after fulfillment has started: " + currentFulfillment.get().getStatus());
        }

        // Release the previous active reservation before creating the new one.
        if (prevAssignment.isPresent()
                && !Set.of("REJECTED", "CANCELLED", "REASSIGNED", "EXPIRED").contains(prevAssignment.get().getStatus())) {
            Long prevRetId = prevAssignment.get().getRetailerId();
            for (OrderItem it : items) {
                String sku = resolveSku(it);
                inventoryService.releaseStock(prevRetId, it.getProductId(), it.getVariantId(), sku,
                        it.getQuantity(), orderId, assignedBy);
            }
            prevAssignment.get().setStatus("REASSIGNED");
            assignmentRepo.save(prevAssignment.get());
            settlementService.markAdjustedForReassignment(orderId, prevRetId, assignedBy);

            currentFulfillment.ifPresent(f -> {
                f.setStatus("REASSIGNED");
                f.setFailedReason("Fulfillment reassigned to another retailer");
                f.setFailedAt(OffsetDateTime.now());
                fulfillmentRepo.save(f);
            });
        }

        for (OrderItem it : items) {
            String sku = resolveSku(it);
            inventoryService.reserveStock(retailerId, it.getProductId(), it.getVariantId(), sku,
                    it.getQuantity(), orderId, assignedBy);
        }

        RetailerOrderAssignment assignment = new RetailerOrderAssignment(orderId, retailerId, assignedBy, notes);
        assignmentRepo.save(assignment);

        // Never retarget an old fulfillment row. A new assignment gets a new fulfillment row.
        // This preserves A -> B -> A history while the DB enforces at most one active fulfillment per order.
        Fulfillment fulfillment = new Fulfillment(orderId, retailerId);
        fulfillment.setStatus("ASSIGNED");
        fulfillment.setCourierName("Wolfe Local Express");
        fulfillmentRepo.save(fulfillment);

        long totalGross = 0, totalWolfeMargin = 0, totalRetailerPayout = 0;
        for (OrderItem it : items) {
            Product p = productRepo.findById(it.getProductId()).orElse(null);
            String cat = p != null ? p.getCategory() : "Hardware";
            long lineGross = it.getLineNetAmount();
            var calc = settlementService.calculateLineItemSettlement(retailerId, cat, it.getProductId(), it.getVariantId(), lineGross);
            totalGross += calc.grossAmount();
            totalWolfeMargin += calc.wolfeMarginAmount();
            totalRetailerPayout += calc.retailerPayableAmount();
        }

        RetailerSettlement settlement = settlementService.initializeSettlement(orderId, retailerId, totalGross, totalWolfeMargin, totalRetailerPayout);
        settlementService.setCashExpected(settlement.getId(), order.getTotal());
        auditRepo.save(new RetailerAuditLog(
                "OrderAllocation", orderId, "ASSIGNED", assignedBy,
                "Order assigned to retailer #" + retailerId + " (" + retailer.getName() + "). Stock reserved."
        ));
    }

    @Transactional
    public int retryUnassignedConfirmedOrders(int maxOrders) {
        int attempted = 0;
        int limit = Math.max(1, Math.min(maxOrders, 100));
        for (Order candidate : orderRepo.findConfirmedAwaitingRetailerAllocation(
                org.springframework.data.domain.PageRequest.of(0, limit, org.springframework.data.domain.Sort.by("createdAt").ascending()))) {
            Order order = orderRepo.findByIdForUpdate(candidate.getId()).orElse(null);
            if (order == null || !"CONFIRMED".equalsIgnoreCase(order.getStatus())) continue;
            if (fulfillmentRepo.findByOrderId(order.getId()).filter(f -> Set.of("ASSIGNED","ACCEPTED","PACKED","READY_FOR_DELIVERY","OUT_FOR_DELIVERY","DELIVERED").contains(f.getStatus().toUpperCase())).isPresent()) continue;
            attempted++;
            allocateOrder(order, orderItemRepo.findByOrderId(order.getId()));
        }
        return attempted;
    }

    @Transactional
    public int expireStaleAssignments(java.time.OffsetDateTime cutoff) {
        int expired = 0;
        for (RetailerOrderAssignment candidate : assignmentRepo.findByStatusAndAssignedAtBefore("ASSIGNED", cutoff)) {
            Order order = orderRepo.findByIdForUpdate(candidate.getOrderId()).orElse(null);
            if (order == null || !"CONFIRMED".equalsIgnoreCase(order.getStatus())) continue;

            // Re-read and lock the assignment after acquiring the order lock. The initial
            // scheduler query is only a candidate list and can be stale: a retailer may have
            // rejected/accepted the assignment between that query and this transaction.
            RetailerOrderAssignment assignment = assignmentRepo.findByIdForUpdate(candidate.getId()).orElse(null);
            if (assignment == null || !"ASSIGNED".equalsIgnoreCase(assignment.getStatus())
                    || assignment.getAssignedAt().isAfter(cutoff)) continue;

            List<OrderItem> items = orderItemRepo.findByOrderId(order.getId());
            for (OrderItem item : items) {
                inventoryService.releaseStock(assignment.getRetailerId(), item.getProductId(), item.getVariantId(),
                        resolveSku(item), item.getQuantity(), order.getId(), "SYSTEM_SLA");
            }
            assignment.setStatus("EXPIRED");
            assignment.setRejectionReason("Retailer acceptance SLA expired");
            assignmentRepo.save(assignment);
            fulfillmentRepo.findByOrderIdForUpdate(order.getId()).ifPresent(f -> {
                if (Set.of("ASSIGNED").contains(f.getStatus().toUpperCase())) {
                    f.setStatus("CANCELLED");
                    f.setFailedReason("Retailer acceptance SLA expired");
                    f.setFailedAt(java.time.OffsetDateTime.now());
                    fulfillmentRepo.save(f);
                }
            });
            auditRepo.save(new RetailerAuditLog("OrderAllocation", order.getId(), "SLA_EXPIRED", "SYSTEM",
                    "Retailer acceptance SLA expired; reservation released"));
            expired++;
        }
        return expired;
    }

    @Transactional(readOnly = true)
    public boolean isGlobalStockStillReserved(String orderId) {
        // Central stock is released exactly once, when a retailer accepts. A later
        // FAILED_DELIVERY/CANCELLED fulfillment can therefore not be used as proof
        // that central stock is still reserved: it may be a historical row after
        // an earlier ACCEPTED/PACKED/DELIVERED fulfillment.
        return fulfillmentRepo.findByOrderIdForUpdateRows(orderId).stream()
                .noneMatch(f -> Set.of("ACCEPTED", "PACKED", "READY_FOR_DELIVERY", "OUT_FOR_DELIVERY", "DELIVERED")
                        .contains(f.getStatus().toUpperCase()));
    }

    @Transactional(readOnly = true)
    public boolean isFulfillmentAcceptedOrBeyond(String orderId) {
        return fulfillmentRepo.findTopByOrderIdOrderByIdDesc(orderId)
                .map(f -> Set.of("ACCEPTED", "PACKED", "READY_FOR_DELIVERY", "OUT_FOR_DELIVERY", "DELIVERED").contains(f.getStatus().toUpperCase()))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean isFulfillmentOutForDelivery(String orderId) {
        return fulfillmentRepo.findTopByOrderIdOrderByIdDesc(orderId)
                .map(f -> Set.of("OUT_FOR_DELIVERY", "DELIVERED").contains(f.getStatus().toUpperCase()))
                .orElse(false);
    }

    @Transactional
    public void acceptOrder(String orderId, Long retailerId) {
        Order lockedOrder = orderRepo.findByIdForUpdate(orderId).orElseThrow(() -> new NoSuchElementException("Order not found"));
        if (Set.of("CANCELLED", "DELIVERED").contains(lockedOrder.getStatus().toUpperCase())) throw new IllegalStateException("Order is already " + lockedOrder.getStatus());
        RetailerOrderAssignment assignment = assignmentRepo.findTopByOrderIdAndRetailerIdOrderByIdDesc(orderId, retailerId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found for order: " + orderId + " and retailer: " + retailerId));
        if (!"ASSIGNED".equalsIgnoreCase(assignment.getStatus())) {
            throw new IllegalStateException("Cannot accept order in status: " + assignment.getStatus());
        }
        assignment.accept();
        assignmentRepo.save(assignment);

        // Transfer stock authority from the central reservation to the retailer only after
        // the retailer has accepted. This releases each central reservation exactly once.
        List<OrderItem> acceptedItems = orderItemRepo.findByOrderId(orderId);
        Map<Long, Integer> centralProducts = new TreeMap<>();
        Map<Long, Integer> centralVariants = new TreeMap<>();
        for (OrderItem item : acceptedItems) {
            if (item.getVariantId() == null) centralProducts.merge(item.getProductId(), item.getQuantity(), Integer::sum);
            else centralVariants.merge(item.getVariantId(), item.getQuantity(), Integer::sum);
        }
        // Keep central stock lock order identical to order creation/cancellation: all
        // variants first, then all product inventory rows. This prevents accept/create
        // cross-transaction deadlocks when an order contains both variant and base-product lines.
        Map<Long, ProductVariant> lockedVariants = new LinkedHashMap<>();
        for (var e : centralVariants.entrySet()) {
            ProductVariant variant = variantRepo.findByIdForUpdate(e.getKey())
                    .orElseThrow(() -> new IllegalStateException("Variant stock record is unavailable"));
            lockedVariants.put(e.getKey(), variant);
        }
        for (var e : centralProducts.entrySet()) {
            Inventory stock = globalInventoryRepo.findByProductIdForUpdate(e.getKey())
                    .orElseThrow(() -> new IllegalStateException("Global inventory missing for product " + e.getKey()));
            stock.release(e.getValue());
            globalInventoryRepo.save(stock);
        }
        for (var e : lockedVariants.entrySet()) {
            ProductVariant variant = e.getValue();
            variant.setStockQuantity(Math.addExact(variant.getStockQuantity(), centralVariants.get(e.getKey())));
            variantRepo.save(variant);
        }

        Fulfillment fulfillment = fulfillmentRepo.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Fulfillment record not found for order: " + orderId));
        if ("DELIVERED".equalsIgnoreCase(fulfillment.getStatus()) || "CANCELLED".equalsIgnoreCase(fulfillment.getStatus())) {
            throw new IllegalStateException("Cannot accept order when fulfillment is: " + fulfillment.getStatus());
        }
        fulfillment.setStatus("ACCEPTED");
        fulfillmentRepo.save(fulfillment);

        Order order = orderRepo.findById(orderId).orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId));
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
        Order lockedOrder = orderRepo.findByIdForUpdate(orderId).orElseThrow(() -> new NoSuchElementException("Order not found"));
        if (Set.of("CANCELLED", "DELIVERED").contains(lockedOrder.getStatus().toUpperCase())) throw new IllegalStateException("Order is already " + lockedOrder.getStatus());
        Fulfillment fulfillment = fulfillmentRepo.findByOrderIdForUpdate(orderId)
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

        Order order = orderRepo.findById(orderId).orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId));
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
        Order lockedOrder = orderRepo.findByIdForUpdate(orderId).orElseThrow(() -> new NoSuchElementException("Order not found"));
        if (Set.of("CANCELLED", "DELIVERED").contains(lockedOrder.getStatus().toUpperCase())) throw new IllegalStateException("Order is already " + lockedOrder.getStatus());
        Fulfillment fulfillment = fulfillmentRepo.findByOrderIdForUpdate(orderId)
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
        Order lockedOrder = orderRepo.findByIdForUpdate(orderId).orElseThrow(() -> new NoSuchElementException("Order not found"));
        if (Set.of("CANCELLED", "DELIVERED").contains(lockedOrder.getStatus().toUpperCase())) throw new IllegalStateException("Order is already " + lockedOrder.getStatus());
        Fulfillment fulfillment = fulfillmentRepo.findByOrderIdForUpdate(orderId)
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

        Order order = orderRepo.findById(orderId).orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId));
        order.setStatus("SHIPPED");
        orderRepo.save(order);

        auditRepo.save(new RetailerAuditLog(
                "Fulfillment", orderId, "OUT_FOR_DELIVERY", "RETAILER_" + retailerId, "Order dispatched out for delivery"
        ));
    }

    @Transactional
    public void deliverOrder(String orderId, Long retailerId) {
        Order lockedOrder = orderRepo.findByIdForUpdate(orderId).orElseThrow(() -> new NoSuchElementException("Order not found"));
        if (!"SHIPPED".equalsIgnoreCase(lockedOrder.getStatus())) throw new IllegalStateException("Order must be SHIPPED before delivery");
        Fulfillment fulfillment = fulfillmentRepo.findByOrderIdForUpdate(orderId)
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

        // Global stock authority was transferred away when the retailer accepted the order.
        // Do not fulfill central stock again here; only the retailer inventory is physically
        // consumed at delivery.
        List<OrderItem> items = orderItemRepo.findByOrderId(orderId).stream()
                .sorted(java.util.Comparator.comparing(OrderItem::getProductId)
                        .thenComparing(i -> i.getVariantId() == null ? Long.MIN_VALUE : i.getVariantId()))
                .toList();
        for (OrderItem it : items) {
            String sku = resolveSku(it);
            inventoryService.fulfillStock(retailerId, it.getProductId(), it.getVariantId(), sku, it.getQuantity(), orderId, "RETAILER_" + retailerId);
        }

        // 3. Mark settlement eligible
        settlementService.markEligible(orderId, retailerId);

        // 4. Mark Wolfe order delivered
        lockedOrder.setStatus("DELIVERED");
        orderRepo.save(lockedOrder);

        auditRepo.save(new RetailerAuditLog(
                "Fulfillment", orderId, "DELIVERED", "RETAILER_" + retailerId, "Order delivered to customer and inventory finalized."
        ));
    }

    @Transactional(readOnly = true)
    public boolean isFulfillmentDelivered(String orderId) {
        return fulfillmentRepo.findByOrderId(orderId).map(f -> "DELIVERED".equalsIgnoreCase(f.getStatus())).orElse(false);
    }

    @Transactional
    public void rejectOrder(String orderId, Long retailerId, String reason) {
        Order lockedOrder = orderRepo.findByIdForUpdate(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found"));
        if (Set.of("CANCELLED", "DELIVERED").contains(lockedOrder.getStatus().toUpperCase())) {
            throw new IllegalStateException("Order is already " + lockedOrder.getStatus());
        }
        RetailerOrderAssignment assignment = assignmentRepo.findTopByOrderIdAndRetailerIdOrderByIdDesc(orderId, retailerId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
        // Acceptance transfers central stock authority to the retailer. A later
        // rejection must not reopen the order while leaving that transfer ambiguous;
        // post-acceptance failures use the explicit fulfillment/reassignment flow.
        if (!"ASSIGNED".equalsIgnoreCase(assignment.getStatus())) {
            throw new IllegalStateException("Cannot reject order in status: " + assignment.getStatus());
        }
        Fulfillment fulfillment = fulfillmentRepo.findByOrderIdForUpdate(orderId).orElse(null);
        if (fulfillment != null && !Objects.equals(fulfillment.getRetailerId(), retailerId)) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized fulfillment update");
        }
        if (fulfillment != null && !"ASSIGNED".equalsIgnoreCase(fulfillment.getStatus())) {
            throw new IllegalStateException("Cannot reject order after acceptance: " + fulfillment.getStatus());
        }
        assignment.reject(reason);
        assignmentRepo.save(assignment);

        List<OrderItem> items = orderItemRepo.findByOrderId(orderId).stream()
                .sorted(java.util.Comparator.comparing(OrderItem::getProductId)
                        .thenComparing(i -> i.getVariantId() == null ? Long.MIN_VALUE : i.getVariantId()))
                .toList();
        // Keep the global order reservation while the order remains active; only the retailer reservation is released.
        for (OrderItem it : items) {
            String sku = resolveSku(it);
            inventoryService.releaseStock(retailerId, it.getProductId(), it.getVariantId(), sku, it.getQuantity(), orderId, "RETAILER_" + retailerId);
        }

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
        if (assignment.isPresent() && !Set.of("CANCELLED", "REJECTED", "REASSIGNED", "COMPLETED").contains(assignment.get().getStatus())) {
            Long retailerId = assignment.get().getRetailerId();
            List<OrderItem> items = orderItemRepo.findByOrderId(orderId).stream()
                    .sorted(Comparator.comparing(OrderItem::getProductId)
                            .thenComparing(i -> i.getVariantId() == null ? Long.MIN_VALUE : i.getVariantId()))
                    .toList();
            for (OrderItem it : items) {
                String sku = resolveSku(it);
                inventoryService.releaseStock(retailerId, it.getProductId(), it.getVariantId(), sku, it.getQuantity(), orderId, "ORDER_CANCELLED");
            }
            assignment.get().setStatus("CANCELLED");
            assignmentRepo.save(assignment.get());
        } else if (assignment.isPresent() && "REJECTED".equals(assignment.get().getStatus())) {
            // Retailer rejection already released its reservation. Cancellation must never release it again.
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
            return it.getVariantSku().trim().toUpperCase(java.util.Locale.ROOT);
        }
        Product p = productRepo.findById(it.getProductId()).orElse(null);
        return p != null ? "SKU-" + p.getSlug().toUpperCase() : "SKU-" + it.getProductId();
    }
    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 6371.0088 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

}

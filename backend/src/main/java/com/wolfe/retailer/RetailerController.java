package com.wolfe.retailer;

import com.wolfe.customer.Customer;
import com.wolfe.customer.CustomerRepository;
import com.wolfe.order.Order;
import com.wolfe.order.OrderItem;
import com.wolfe.order.OrderItemRepository;
import com.wolfe.order.OrderRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/retailer")
public class RetailerController {
    private final RetailerRepository retailerRepo;
    private final RetailerInventoryRepository inventoryRepo;
    private final RetailerInventoryService inventoryService;
    private final InventoryMovementRepository movementRepo;
    private final RetailerOrderAssignmentRepository assignmentRepo;
    private final FulfillmentRepository fulfillmentRepo;
    private final RetailerAllocationService allocationService;
    private final RetailerSettlementRepository settlementRepo;
    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final CustomerRepository customerRepo;

    public RetailerController(RetailerRepository retailerRepo,
                              RetailerInventoryRepository inventoryRepo,
                              RetailerInventoryService inventoryService,
                              InventoryMovementRepository movementRepo,
                              RetailerOrderAssignmentRepository assignmentRepo,
                              FulfillmentRepository fulfillmentRepo,
                              RetailerAllocationService allocationService,
                              RetailerSettlementRepository settlementRepo,
                              OrderRepository orderRepo,
                              OrderItemRepository orderItemRepo,
                              CustomerRepository customerRepo) {
        this.retailerRepo = retailerRepo;
        this.inventoryRepo = inventoryRepo;
        this.inventoryService = inventoryService;
        this.movementRepo = movementRepo;
        this.assignmentRepo = assignmentRepo;
        this.fulfillmentRepo = fulfillmentRepo;
        this.allocationService = allocationService;
        this.settlementRepo = settlementRepo;
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.customerRepo = customerRepo;
    }

    private Retailer getAuthenticatedRetailer(Authentication auth) {
        if (auth == null || auth.getDetails() == null) {
            throw new AccessDeniedException("Authentication required");
        }
        Long customerId = (Long) auth.getDetails();

        // 1. Try find by userId
        Optional<Retailer> ret = retailerRepo.findByUserId(customerId);
        if (ret.isPresent()) return ret.get();

        // 2. Try find by email
        Customer c = customerRepo.findById(customerId).orElse(null);
        if (c != null) {
            Optional<Retailer> byEmail = retailerRepo.findByEmail(c.getEmail());
            if (byEmail.isPresent()) return byEmail.get();
        }

        // 3. If admin role, allow fallback to first active retailer for demo/dashboard
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("ADMIN"));
        if (isAdmin) {
            return retailerRepo.findAll().stream().findFirst()
                    .orElseThrow(() -> new AccessDeniedException("No retailer partner configured"));
        }

        throw new AccessDeniedException("No retailer partner linked to this account");
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication auth) {
        Retailer r = getAuthenticatedRetailer(auth);
        List<RetailerInventory> lowStock = inventoryRepo.findLowStockItems(r.getId());
        List<RetailerOrderAssignment> pending = assignmentRepo.findByRetailerIdAndStatus(r.getId(), "ASSIGNED");
        List<RetailerSettlement> settlements = settlementRepo.findByRetailerId(r.getId());

        long totalEarned = settlements.stream()
                .filter(s -> "SETTLED".equals(s.getStatus()) || "ELIGIBLE".equals(s.getStatus()))
                .mapToLong(RetailerSettlement::getRetailerPayableAmount)
                .sum();

        return Map.of(
                "retailer", r,
                "stats", Map.of(
                        "lowStockCount", lowStock.size(),
                        "pendingOrdersCount", pending.size(),
                        "totalSettlements", settlements.size(),
                        "totalEarningsPaise", totalEarned
                )
        );
    }

    @GetMapping("/orders")
    public List<Map<String, Object>> getAssignedOrders(Authentication auth) {
        Retailer r = getAuthenticatedRetailer(auth);
        List<RetailerOrderAssignment> assignments = assignmentRepo.findByRetailerId(r.getId());

        List<Map<String, Object>> result = new ArrayList<>();
        for (RetailerOrderAssignment assign : assignments) {
            Order order = orderRepo.findById(assign.getOrderId()).orElse(null);
            if (order == null) continue;

            List<OrderItem> items = orderItemRepo.findByOrderId(order.getId());
            Fulfillment fulfillment = fulfillmentRepo.findByOrderIdAndRetailerId(order.getId(), r.getId()).orElse(null);
            RetailerSettlement settlement = settlementRepo.findByOrderIdAndRetailerId(order.getId(), r.getId()).orElse(null);

            result.add(Map.of(
                    "assignment", assign,
                    "orderId", order.getId(),
                    "orderCreatedAt", order.getCreatedAt(),
                    "shippingMethod", order.getShippingMethod(),
                    "customerDelivery", Map.of(
                            "name", order.getCustomerName(),
                            "phone", order.getPhone(),
                            "address", order.getAddress(),
                            "city", order.getCity(),
                            "pincode", order.getPincode()
                    ),
                    "items", items,
                    "fulfillment", fulfillment != null ? fulfillment : Map.of("status", assign.getStatus()),
                    "settlement", settlement != null ? Map.of(
                            "retailerPayableAmount", settlement.getRetailerPayableAmount(),
                            "status", settlement.getStatus()
                    ) : Map.of()
            ));
        }

        return result;
    }

    @PostMapping("/orders/{orderId}/accept")
    public Map<String, Object> acceptOrder(@PathVariable String orderId, Authentication auth) {
        Retailer r = getAuthenticatedRetailer(auth);
        allocationService.acceptOrder(orderId, r.getId());
        return Map.of("success", true, "orderId", orderId, "status", "ACCEPTED");
    }

    public record PackRequest(String trackingNumber, String courierName) {}

    @PostMapping("/orders/{orderId}/pack")
    public Map<String, Object> packOrder(@PathVariable String orderId, @RequestBody(required = false) PackRequest req, Authentication auth) {
        Retailer r = getAuthenticatedRetailer(auth);
        String tracking = req != null ? req.trackingNumber() : null;
        String courier = req != null ? req.courierName() : null;
        allocationService.packOrder(orderId, r.getId(), tracking, courier);
        return Map.of("success", true, "orderId", orderId, "status", "PACKED");
    }

    @PostMapping("/orders/{orderId}/ready")
    public Map<String, Object> markReady(@PathVariable String orderId, Authentication auth) {
        Retailer r = getAuthenticatedRetailer(auth);
        allocationService.markReady(orderId, r.getId());
        return Map.of("success", true, "orderId", orderId, "status", "READY_FOR_DELIVERY");
    }

    @PostMapping("/orders/{orderId}/out-for-delivery")
    public Map<String, Object> outForDelivery(@PathVariable String orderId, @RequestBody(required = false) PackRequest req, Authentication auth) {
        Retailer r = getAuthenticatedRetailer(auth);
        String tracking = req != null ? req.trackingNumber() : null;
        allocationService.outForDelivery(orderId, r.getId(), tracking);
        return Map.of("success", true, "orderId", orderId, "status", "OUT_FOR_DELIVERY");
    }

    @PostMapping("/orders/{orderId}/deliver")
    public Map<String, Object> deliverOrder(@PathVariable String orderId, Authentication auth) {
        Retailer r = getAuthenticatedRetailer(auth);
        allocationService.deliverOrder(orderId, r.getId());
        return Map.of("success", true, "orderId", orderId, "status", "DELIVERED");
    }

    public record RejectRequest(@NotBlank String reason) {}

    @PostMapping("/orders/{orderId}/reject")
    public Map<String, Object> rejectOrder(@PathVariable String orderId, @Valid @RequestBody RejectRequest req, Authentication auth) {
        Retailer r = getAuthenticatedRetailer(auth);
        allocationService.rejectOrder(orderId, r.getId(), req.reason());
        return Map.of("success", true, "orderId", orderId, "status", "REJECTED");
    }

    @GetMapping("/inventory")
    public Page<RetailerInventory> getInventory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String query,
            Authentication auth
    ) {
        Retailer r = getAuthenticatedRetailer(auth);
        return inventoryRepo.searchByRetailer(r.getId(), query, PageRequest.of(page, size, Sort.by("id").ascending()));
    }

    public record AdjustStockRequest(@NotBlank String sku, @NotNull Integer newPhysicalStock, String reason) {}

    @PostMapping("/inventory/adjust")
    public RetailerInventory adjustStock(@Valid @RequestBody AdjustStockRequest req, Authentication auth) {
        Retailer r = getAuthenticatedRetailer(auth);
        String reason = req.reason() != null && !req.reason().isBlank() ? req.reason() : "Retailer self-service stock adjustment";
        return inventoryService.adjustStock(r.getId(), req.sku(), req.newPhysicalStock(), "RETAILER_" + r.getId(), reason);
    }

    @GetMapping("/inventory/movements")
    public Page<InventoryMovement> getMovements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            Authentication auth
    ) {
        Retailer r = getAuthenticatedRetailer(auth);
        return movementRepo.findByRetailerIdOrderByCreatedAtDesc(r.getId(), PageRequest.of(page, size));
    }

    @GetMapping("/settlements")
    public Page<RetailerSettlement> getSettlements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            Authentication auth
    ) {
        Retailer r = getAuthenticatedRetailer(auth);
        return settlementRepo.searchSettlements(r.getId(), status, PageRequest.of(page, size));
    }
}

package com.wolfe.retailer;

import com.wolfe.customer.Customer;
import com.wolfe.customer.CustomerRepository;
import com.wolfe.order.Order;
import com.wolfe.order.OrderItem;
import com.wolfe.order.OrderItemRepository;
import com.wolfe.order.OrderRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
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
        if (auth == null || auth.getDetails() == null
                || auth.getAuthorities().stream().noneMatch(a -> "ROLE_RETAILER".equals(a.getAuthority()))) {
            throw new AccessDeniedException("Authentication required");
        }
        Long customerId = (Long) auth.getDetails();
        Retailer r = retailerRepo.findByUserId(customerId)
                .orElseThrow(() -> new AccessDeniedException("No retailer partner linked to this account"));
        if (!r.isActive()) throw new AccessDeniedException("Retailer account is not active or verified");
        return r;
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication auth) {
        Retailer r = getAuthenticatedRetailer(auth);
        List<RetailerInventory> lowStock = inventoryRepo.findLowStockItems(r.getId());
        List<RetailerOrderAssignment> pending = assignmentRepo.findByRetailerIdAndStatus(r.getId(), "ASSIGNED");
        List<RetailerSettlement> settlements = settlementRepo.findByRetailerId(r.getId());

        long totalEarned = settlements.stream()
                .mapToLong(s -> {
                    if ("SETTLED".equalsIgnoreCase(s.getStatus())) return s.getSettledAmount();
                    if ("ELIGIBLE".equalsIgnoreCase(s.getStatus())) {
                        return Math.max(0L, s.getRetailerPayableAmount() - s.getAdjustmentAmount());
                    }
                    return 0L;
                })
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
        List<RetailerOrderAssignment> assignments = assignmentRepo.findByRetailerIdAndStatus(r.getId(), "ASSIGNED");
        assignments.addAll(assignmentRepo.findByRetailerIdAndStatus(r.getId(), "ACCEPTED"));
        assignments = assignments.stream().filter(a -> Set.of("ASSIGNED", "ACCEPTED").contains(a.getStatus().toUpperCase())).toList();

        List<Map<String, Object>> result = new ArrayList<>();
        for (RetailerOrderAssignment assign : assignments) {
            Order order = orderRepo.findById(assign.getOrderId()).orElse(null);
            if (order == null) continue;

            List<OrderItem> items = orderItemRepo.findByOrderId(order.getId());
            Fulfillment fulfillment = fulfillmentRepo.findTopByOrderIdAndRetailerIdOrderByIdDesc(order.getId(), r.getId()).orElse(null);
            RetailerSettlement settlement = settlementRepo.findTopByOrderIdAndRetailerIdOrderByIdDesc(order.getId(), r.getId()).orElse(null);

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

    public record PackRequest(@Size(max = 100) String trackingNumber, @Size(max = 100) String courierName) {}

    @PostMapping("/orders/{orderId}/pack")
    public Map<String, Object> packOrder(@PathVariable String orderId, @Valid @RequestBody(required = false) PackRequest req, Authentication auth) {
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
    public Map<String, Object> outForDelivery(@PathVariable String orderId, @Valid @RequestBody(required = false) PackRequest req, Authentication auth) {
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
        return inventoryRepo.searchByRetailer(r.getId(), query, PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100), Sort.by("id").ascending()));
    }

    public record AdjustStockRequest(@NotBlank @Size(max = 100) String sku, @NotNull @Min(0) Integer newPhysicalStock, @Size(max = 500) String reason) {}

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
        return movementRepo.findByRetailerIdOrderByCreatedAtDesc(r.getId(), PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100)));
    }

    @GetMapping("/settlements")
    public Page<RetailerSettlement> getSettlements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            Authentication auth
    ) {
        Retailer r = getAuthenticatedRetailer(auth);
        return settlementRepo.searchSettlements(r.getId(), status, PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100)));
    }
}

package com.wolfe.retailer;

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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/v1/admin/retailers")
public class AdminRetailerController {
    private final RetailerRepository retailerRepo;
    private final RetailerServiceAreaRepository serviceAreaRepo;
    private final RetailerInventoryRepository inventoryRepo;
    private final RetailerInventoryService inventoryService;
    private final InventoryMovementRepository movementRepo;
    private final RetailerOrderAssignmentRepository assignmentRepo;
    private final FulfillmentRepository fulfillmentRepo;
    private final RetailerMarginRuleRepository marginRepo;
    private final RetailerSettlementRepository settlementRepo;
    private final RetailerSettlementService settlementService;
    private final RetailerAllocationService allocationService;
    private final RetailerAuditLogRepository auditRepo;
    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;

    public AdminRetailerController(RetailerRepository retailerRepo,
                                  RetailerServiceAreaRepository serviceAreaRepo,
                                  RetailerInventoryRepository inventoryRepo,
                                  RetailerInventoryService inventoryService,
                                  InventoryMovementRepository movementRepo,
                                  RetailerOrderAssignmentRepository assignmentRepo,
                                  FulfillmentRepository fulfillmentRepo,
                                  RetailerMarginRuleRepository marginRepo,
                                  RetailerSettlementRepository settlementRepo,
                                  RetailerSettlementService settlementService,
                                  RetailerAllocationService allocationService,
                                  RetailerAuditLogRepository auditRepo,
                                  OrderRepository orderRepo,
                                  OrderItemRepository orderItemRepo) {
        this.retailerRepo = retailerRepo;
        this.serviceAreaRepo = serviceAreaRepo;
        this.inventoryRepo = inventoryRepo;
        this.inventoryService = inventoryService;
        this.movementRepo = movementRepo;
        this.assignmentRepo = assignmentRepo;
        this.fulfillmentRepo = fulfillmentRepo;
        this.marginRepo = marginRepo;
        this.settlementRepo = settlementRepo;
        this.settlementService = settlementService;
        this.allocationService = allocationService;
        this.auditRepo = auditRepo;
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
    }

    // 1. Retailer Directory & Management
    @GetMapping
    public Page<Retailer> listRetailers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status
    ) {
        return retailerRepo.searchRetailers(query, status, PageRequest.of(page, size, Sort.by("id").ascending()));
    }

    @GetMapping("/{id}")
    public Map<String, Object> getRetailer(@PathVariable Long id) {
        Retailer r = retailerRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Retailer not found: " + id));
        List<RetailerServiceArea> areas = serviceAreaRepo.findByRetailerId(id);
        List<RetailerMarginRule> rules = marginRepo.findByRetailerId(id);
        List<RetailerInventory> lowStock = inventoryRepo.findLowStockItems(id);

        return Map.of(
                "retailer", r,
                "serviceAreas", areas,
                "marginRules", rules,
                "lowStockItems", lowStock
        );
    }

    public record CreateRetailerRequest(
            @NotBlank String name,
            String ownerName,
            @NotBlank String email,
            @NotBlank String phone,
            @NotBlank String address,
            @NotBlank String city,
            String state,
            @NotBlank String pincode,
            BigDecimal deliveryRadiusKm,
            BigDecimal commissionRate
    ) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Retailer createRetailer(@Valid @RequestBody CreateRetailerRequest req, Authentication auth) {
        Retailer r = new Retailer(req.name(), req.ownerName(), req.email(), req.phone(), req.address(), req.city(), req.state(), req.pincode());
        if (req.deliveryRadiusKm() != null) r.setDeliveryRadiusKm(req.deliveryRadiusKm());
        if (req.commissionRate() != null) r.setCommissionRate(req.commissionRate());

        Retailer saved = retailerRepo.save(r);
        auditRepo.save(new RetailerAuditLog(
                "Retailer", String.valueOf(saved.getId()),
                "CREATE_RETAILER", auth != null ? auth.getName() : "ADMIN", "Added partner: " + saved.getName()
        ));
        return saved;
    }

    public record UpdateRetailerRequest(
            String name,
            String ownerName,
            String phone,
            String address,
            String city,
            String state,
            String pincode,
            BigDecimal deliveryRadiusKm,
            String status,
            String verificationStatus,
            String agreementStatus,
            BigDecimal commissionRate
    ) {}

    @PutMapping("/{id}")
    public Retailer updateRetailer(@PathVariable Long id, @RequestBody UpdateRetailerRequest req, Authentication auth) {
        Retailer r = retailerRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Retailer not found"));
        if (req.name() != null) r.setName(req.name());
        if (req.ownerName() != null) r.setOwnerName(req.ownerName());
        if (req.phone() != null) r.setPhone(req.phone());
        if (req.address() != null) r.setAddress(req.address());
        if (req.city() != null) r.setCity(req.city());
        if (req.state() != null) r.setState(req.state());
        if (req.pincode() != null) r.setPincode(req.pincode());
        if (req.deliveryRadiusKm() != null) r.setDeliveryRadiusKm(req.deliveryRadiusKm());
        if (req.status() != null) r.setStatus(req.status().toUpperCase());
        if (req.verificationStatus() != null) r.setVerificationStatus(req.verificationStatus().toUpperCase());
        if (req.agreementStatus() != null) r.setAgreementStatus(req.agreementStatus().toUpperCase());
        if (req.commissionRate() != null) r.setCommissionRate(req.commissionRate());

        Retailer saved = retailerRepo.save(r);
        auditRepo.save(new RetailerAuditLog(
                "Retailer", String.valueOf(saved.getId()),
                "UPDATE_RETAILER", auth != null ? auth.getName() : "ADMIN", "Updated partner details for: " + saved.getName()
        ));
        return saved;
    }

    // 2. Service Areas
    public record AddServiceAreaRequest(@NotBlank String pincode, @NotBlank String city, String areaName, Integer deliveryEtaHours) {}

    @PostMapping("/{id}/service-areas")
    public RetailerServiceArea addServiceArea(@PathVariable Long id, @Valid @RequestBody AddServiceAreaRequest req) {
        RetailerServiceArea area = new RetailerServiceArea(id, req.pincode(), req.city(), req.areaName(), req.deliveryEtaHours() != null ? req.deliveryEtaHours() : 24);
        return serviceAreaRepo.save(area);
    }

    @DeleteMapping("/service-areas/{areaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeServiceArea(@PathVariable Long areaId) {
        serviceAreaRepo.deleteById(areaId);
    }

    // 3. Inventory Inspection & Adjustment
    @GetMapping("/{id}/inventory")
    public Page<RetailerInventory> getRetailerInventory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String query
    ) {
        return inventoryRepo.searchByRetailer(id, query, PageRequest.of(page, size, Sort.by("id").ascending()));
    }

    public record AdminAdjustStockRequest(@NotBlank String sku, @NotNull Integer newPhysicalStock, @NotBlank String reason) {}

    @PostMapping("/{id}/inventory/adjust")
    public RetailerInventory adjustRetailerStock(@PathVariable Long id, @Valid @RequestBody AdminAdjustStockRequest req, Authentication auth) {
        return inventoryService.adjustStock(id, req.sku(), req.newPhysicalStock(), auth != null ? auth.getName() : "ADMIN", req.reason());
    }

    // 4. Order Allocation Dashboard & Manual Override
    @GetMapping("/allocations/eval/{orderId}")
    public Map<String, Object> evaluateAllocation(@PathVariable String orderId) {
        Order order = orderRepo.findById(orderId).orElseThrow(() -> new NoSuchElementException("Order not found"));
        List<OrderItem> items = orderItemRepo.findByOrderId(orderId);
        List<RetailerAllocationService.AllocationCandidate> candidates = allocationService.evaluateEligibleRetailers(orderId);
        Optional<RetailerOrderAssignment> currentAssignment = assignmentRepo.findTopByOrderIdOrderByIdDesc(orderId);
        Fulfillment fulfillment = fulfillmentRepo.findByOrderId(orderId).orElse(null);

        return Map.of(
                "order", order,
                "items", items,
                "currentAssignment", currentAssignment.orElse(null),
                "fulfillment", fulfillment != null ? fulfillment : Map.of(),
                "candidates", candidates
        );
    }

    public record AssignOrderRequest(@NotNull Long retailerId, String notes) {}

    @PostMapping("/allocations/{orderId}/assign")
    public Map<String, Object> manualAssignOrder(@PathVariable String orderId, @Valid @RequestBody AssignOrderRequest req, Authentication auth) {
        allocationService.assignToRetailer(orderId, req.retailerId(), auth != null ? auth.getName() : "ADMIN_MANUAL", req.notes());
        return Map.of("success", true, "orderId", orderId, "retailerId", req.retailerId(), "status", "ASSIGNED");
    }

    // 5. Margin Rules Management
    @GetMapping("/margin-rules")
    public List<RetailerMarginRule> listMarginRules() {
        return marginRepo.findByActiveTrueOrderByPriorityDesc();
    }

    public record CreateMarginRuleRequest(Long retailerId, String category, Long productId, Long variantId, String marginType, @NotNull BigDecimal marginValue, Integer priority) {}

    @PostMapping("/margin-rules")
    @ResponseStatus(HttpStatus.CREATED)
    public RetailerMarginRule createMarginRule(@Valid @RequestBody CreateMarginRuleRequest req) {
        RetailerMarginRule rule = new RetailerMarginRule(
                req.retailerId(), req.category(), req.productId(), req.variantId(),
                req.marginType(), req.marginValue(), req.priority() != null ? req.priority() : 0
        );
        return marginRepo.save(rule);
    }

    @DeleteMapping("/margin-rules/{ruleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMarginRule(@PathVariable Long ruleId) {
        marginRepo.deleteById(ruleId);
    }

    // 6. Settlements Management
    @GetMapping("/settlements")
    public Page<RetailerSettlement> listSettlements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long retailerId,
            @RequestParam(required = false) String status
    ) {
        return settlementRepo.searchSettlements(retailerId, status, PageRequest.of(page, size));
    }

    public record SettleRequest(@NotBlank String referenceNumber) {}

    @PostMapping("/settlements/{id}/settle")
    public RetailerSettlement settlePayment(@PathVariable Long id, @Valid @RequestBody SettleRequest req, Authentication auth) {
        return settlementService.markSettled(id, req.referenceNumber(), auth != null ? auth.getName() : "ADMIN");
    }

    // 7. Audit Logs
    @GetMapping("/audit-logs")
    public Page<RetailerAuditLog> listAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        return auditRepo.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
    }
}

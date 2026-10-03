package com.wolfe.returning;

import com.wolfe.order.*;
import com.wolfe.retailer.Fulfillment;
import com.wolfe.retailer.FulfillmentRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import java.time.Instant;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import static com.wolfe.security.CustomerAccess.requireCustomer;

@RestController
@RequestMapping("/api/v1/customers/{customerId}/returns")
public class ReturnRequestController {
    private final ReturnRequestRepository returns;
    private final OrderRepository orders;
    private final OrderItemRepository items;
    private final ObjectMapper mapper;
    private final FulfillmentRepository fulfillments;
    public ReturnRequestController(ReturnRequestRepository returns, OrderRepository orders, OrderItemRepository items, ObjectMapper mapper, FulfillmentRepository fulfillments) { this.returns=returns; this.orders=orders; this.items=items; this.mapper=mapper; this.fulfillments=fulfillments; }
    public record ReturnItem(Long orderItemId, @Min(0) int quantity) {}
    public record ReturnView(Long id, String orderId, String reason, String itemQuantitiesJson, String status, long refundAmount, String refundStatus, Instant createdAt, Instant updatedAt, String restockStatus, String settlementAdjustmentStatus, Instant completedAt) {
        ReturnView(ReturnRequest r) { this(r.getId(), r.getOrderId(), r.getReason(), r.getItemQuantitiesJson(), r.getStatus(), r.getRefundAmount(), r.getRefundStatus(), r.getCreatedAt(), r.getUpdatedAt(), r.getRestockStatus(), r.getSettlementAdjustmentStatus(), r.getCompletedAt()); }
    }
    public record CreateReturn(@NotBlank @Size(max = 1000) String reason, @Size(max = 100) List<ReturnItem> items) {
    }
    @GetMapping public List<ReturnView> list(@PathVariable Long customerId, Authentication auth) {
        requireCustomer(auth, customerId);
        return returns.findByCustomerIdOrderByCreatedAtDesc(customerId).stream().map(ReturnView::new).toList();
    }
    @PostMapping("/{orderId}")
    @ResponseStatus(HttpStatus.CREATED) @Transactional
    public ReturnView create(@PathVariable Long customerId, @PathVariable String orderId,
    @Valid @RequestBody CreateReturn r,
    Authentication auth) {
        requireCustomer(auth, customerId);
        Order o = orders.findByIdForUpdate(orderId).orElseThrow(() -> new NoSuchElementException("Order not found"));
        if (!o.getCustomerId().equals(customerId))throw new org.springframework.security.access.AccessDeniedException("Order access denied");
        if (!"DELIVERED".equals(o.getStatus()))throw new IllegalArgumentException("Returns can be requested after delivery");
        var fulfillment = fulfillments.findTopByOrderIdOrderByIdDesc(orderId)
                .filter(f -> "DELIVERED".equalsIgnoreCase(f.getStatus()) && f.getDeliveredAt() != null)
                .orElseThrow(() -> new IllegalStateException("Delivery timestamp is required before requesting a return"));
        if (fulfillment.getDeliveredAt().plusDays(30).isBefore(java.time.OffsetDateTime.now())) {
            throw new IllegalArgumentException("Return window has expired");
        }
        var existingReturns = returns.findAllByOrderIdOrderByCreatedAtAsc(orderId).stream()
                .filter(x -> !"REJECTED".equalsIgnoreCase(x.getStatus()))
                .toList();
        var orderItems = items.findByOrderId(orderId);
        Map<Long,Integer> requested = new LinkedHashMap<>();
        if (r.items() == null || r.items().isEmpty()) { for (var i : orderItems) requested.put(i.getId(), i.getQuantity()); }
        else {
            Set<Long> validItemIds = orderItems.stream().map(OrderItem::getId).collect(java.util.stream.Collectors.toSet());
            for (var x : r.items()) {
                if (x.orderItemId() == null || x.quantity() <= 0) throw new IllegalArgumentException("Return quantity must be positive");
                if (!validItemIds.contains(x.orderItemId())) throw new IllegalArgumentException("Return item does not belong to this order");
                requested.merge(x.orderItemId(), x.quantity(), Integer::sum);
            }
            if (requested.isEmpty()) throw new IllegalArgumentException("At least one item must be returned");
        }
        Map<Long,Integer> alreadyReturned = new HashMap<>();
        for (var prior : existingReturns) {
            try {
                Map<Long,Integer> priorItems = mapper.readValue(prior.getItemQuantitiesJson(), new com.fasterxml.jackson.core.type.TypeReference<Map<Long,Integer>>(){});
                priorItems.forEach((k,v) -> alreadyReturned.merge(k, v, Integer::sum));
            } catch (Exception e) { throw new IllegalStateException("Existing return data is invalid"); }
        }
        for (var i : orderItems) {
            int q = requested.getOrDefault(i.getId(), 0);
            if (q + alreadyReturned.getOrDefault(i.getId(), 0) > i.getQuantity()) throw new IllegalArgumentException("Return quantity exceeds remaining returnable quantity for item " + i.getId());
        }
        try { var rr = new ReturnRequest(orderId, customerId, r.reason()); rr.setItemQuantitiesJson(mapper.writeValueAsString(requested)); return new ReturnView(returns.save(rr)); }
        catch (Exception e) { throw new IllegalArgumentException("Invalid return item selection", e); }
    }
}

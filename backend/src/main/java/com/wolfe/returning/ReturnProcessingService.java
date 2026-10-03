package com.wolfe.returning;

import com.wolfe.inventory.InventoryRepository;
import com.wolfe.catalog.ProductRepository;
import com.wolfe.catalog.ProductVariant;
import com.wolfe.catalog.ProductVariantRepository;
import com.wolfe.order.OrderItem;
import com.wolfe.order.OrderItemRepository;
import com.wolfe.order.OrderRepository;
import com.wolfe.retailer.*;
import org.springframework.stereotype.Service;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ReturnProcessingService {
    private final ReturnRequestRepository returns;
    private final OrderRepository orders;
    private final OrderItemRepository items;
    private final InventoryRepository globalInventory;
    private final FulfillmentRepository fulfillments;
    private final RetailerInventoryService retailerInventory;
    private final RetailerSettlementService settlements;
    private final ProductRepository products;
    private final ProductVariantRepository variants;
    private final ObjectMapper mapper;
    private final com.wolfe.notification.NotificationService notifications;

    public ReturnProcessingService(ReturnRequestRepository returns, OrderRepository orders, OrderItemRepository items,
                                   InventoryRepository globalInventory, FulfillmentRepository fulfillments,
                                   RetailerInventoryService retailerInventory, RetailerSettlementService settlements, ProductRepository products, ProductVariantRepository variants, ObjectMapper mapper, com.wolfe.notification.NotificationService notifications) {
        this.returns = returns; this.orders = orders; this.items = items; this.globalInventory = globalInventory; this.products = products; this.variants = variants;
        this.fulfillments = fulfillments; this.retailerInventory = retailerInventory; this.settlements = settlements; this.mapper = mapper; this.notifications = notifications;
    }

    public long maxRefundableAmount(ReturnRequest rr, com.wolfe.order.Order order) {
        Map<Long,Integer> selected;
        try {
            selected = rr.getItemQuantitiesJson() == null || rr.getItemQuantitiesJson().isBlank()
                    ? Map.of()
                    : mapper.readValue(rr.getItemQuantitiesJson(), new TypeReference<Map<Long,Integer>>(){});
        } catch (Exception e) {
            throw new IllegalStateException("Invalid return item selection", e);
        }
        var orderItems = items.findByOrderId(order.getId());
        if (!selected.isEmpty()) {
            var validIds = orderItems.stream().map(OrderItem::getId).collect(java.util.stream.Collectors.toSet());
            if (selected.keySet().stream().anyMatch(id -> !validIds.contains(id))) {
                throw new IllegalStateException("Return contains an item that does not belong to the order");
            }
        }
        long selectedNet = 0L;
        boolean fullReturn = selected.isEmpty();
        for (OrderItem item : orderItems) {
            int q = selected.isEmpty() ? item.getQuantity() : selected.getOrDefault(item.getId(), 0);
            if (q < 0 || q > item.getQuantity()) throw new IllegalStateException("Return quantity exceeds ordered quantity for item " + item.getId());
            if (q != item.getQuantity()) fullReturn = false;
            if (q > 0) {
                long lineRefund = java.math.BigDecimal.valueOf(item.getLineNetAmount())
                        .multiply(java.math.BigDecimal.valueOf(q))
                        .divide(java.math.BigDecimal.valueOf(item.getQuantity()), 0, java.math.RoundingMode.HALF_UP)
                        .longValueExact();
                selectedNet = Math.addExact(selectedNet, lineRefund);
            }
        }
        if (selectedNet <= 0) throw new IllegalStateException("Return must contain at least one item");
        // Line net amounts already include bundle and coupon discounts. For a full return,
        // the order total is the authoritative ceiling; do not reconstruct shipping by
        // subtracting subtotal because that would subtract coupon discount twice.
        long candidate = fullReturn ? order.getTotal() : selectedNet;

        // Multiple partial returns are allowed, so the order-level refund ceiling must be
        // cumulative as well. Without this guard, per-line HALF_UP rounding can make several
        // legitimate-looking partial returns exceed the original order total by a paise (or
        // more when shipping is included). Only already-paid refunds consume the financial
        // ceiling; FAILED/PENDING refunds are still recoverable/retryable states.
        long alreadyRefunded = returns.findAllByOrderIdOrderByCreatedAtAsc(order.getId()).stream()
                .filter(existing -> existing.getId() != null && !existing.getId().equals(rr.getId()))
                .filter(existing -> "REFUNDED".equalsIgnoreCase(existing.getRefundStatus()))
                .mapToLong(ReturnRequest::getRefundAmount)
                .reduce(0L, Math::addExact);
        return Math.min(candidate, Math.max(0L, Math.subtractExact(order.getTotal(), alreadyRefunded)));
    }

    @Transactional
    public ReturnRequest complete(Long returnId, long refundAmount, String actor) {
        ReturnRequest rr = returns.findByIdForUpdate(returnId).orElseThrow(() -> new java.util.NoSuchElementException("Return request not found"));
        if (!"COMPLETED".equalsIgnoreCase(rr.getStatus())) throw new IllegalStateException("Return must be marked COMPLETED before processing");
        if (!"REFUNDED".equalsIgnoreCase(rr.getRefundStatus())) throw new IllegalStateException("Return refund must be REFUNDED before inventory settlement");
        if ("RESTOCKED".equalsIgnoreCase(rr.getRestockStatus()) && "ADJUSTED".equalsIgnoreCase(rr.getSettlementAdjustmentStatus())) return rr;
        var order = orders.findByIdForUpdate(rr.getOrderId()).orElseThrow(() -> new java.util.NoSuchElementException("Order not found"));
        if (!"DELIVERED".equalsIgnoreCase(order.getStatus())) throw new IllegalStateException("Only delivered orders can be completed as returned");
        if (rr.getRefundAmount() <= 0 || rr.getRefundAmount() > order.getTotal()) throw new IllegalStateException("Return refund amount is outside the order total");
        Long retailerId = fulfillments.findByOrderIdForUpdate(order.getId()).map(Fulfillment::getRetailerId).orElse(null);
        Map<Long,Integer> selected;
        try { selected = rr.getItemQuantitiesJson() == null || rr.getItemQuantitiesJson().isBlank() ? Map.of() : mapper.readValue(rr.getItemQuantitiesJson(), new TypeReference<Map<Long,Integer>>(){}); }
        catch (Exception e) { throw new IllegalStateException("Invalid return item selection", e); }
        var orderItems = items.findByOrderId(order.getId()).stream()
                .sorted(java.util.Comparator.comparing(OrderItem::getProductId)
                        .thenComparing(i -> i.getVariantId() == null ? Long.MIN_VALUE : i.getVariantId())
                        .thenComparing(OrderItem::getId))
                .toList();
        if (!selected.isEmpty()) {
            var validIds = orderItems.stream().map(OrderItem::getId).collect(java.util.stream.Collectors.toSet());
            if (selected.keySet().stream().anyMatch(id -> !validIds.contains(id))) {
                throw new IllegalStateException("Return contains an item that does not belong to the order");
            }
        }

        // Central stock uses the same global lock order as order creation, cancellation,
        // and retailer acceptance: all variant rows first, then all product inventory rows.
        // Never interleave variant/inventory locks item-by-item because two different orders
        // could otherwise acquire the same resources in opposite orders.
        Map<Long, Integer> selectedVariants = new java.util.TreeMap<>();
        Map<Long, Integer> selectedProducts = new java.util.TreeMap<>();
        for (OrderItem item : orderItems) {
            int quantity = selected.isEmpty() ? item.getQuantity() : selected.getOrDefault(item.getId(), 0);
            if (quantity <= 0) continue;
            if (quantity > item.getQuantity()) throw new IllegalStateException("Return quantity exceeds ordered quantity for item " + item.getId());
            if (retailerId == null) {
                if (item.getVariantId() != null) selectedVariants.merge(item.getVariantId(), quantity, Integer::sum);
                else selectedProducts.merge(item.getProductId(), quantity, Integer::sum);
            }
        }
        Map<Long, ProductVariant> lockedVariants = new java.util.LinkedHashMap<>();
        if (retailerId == null) {
            for (var e : selectedVariants.entrySet()) {
                ProductVariant variant = variants.findByIdForUpdate(e.getKey())
                        .orElseThrow(() -> new IllegalStateException("Variant stock record is unavailable"));
                lockedVariants.put(e.getKey(), variant);
            }
            for (var e : selectedProducts.entrySet()) {
                var stock = globalInventory.findByProductIdForUpdate(e.getKey())
                        .orElseThrow(() -> new IllegalStateException("Global inventory missing for product " + e.getKey()));
                stock.setQuantity(Math.addExact(stock.getQuantity(), e.getValue()));
                globalInventory.save(stock);
            }
            for (var e : lockedVariants.entrySet()) {
                ProductVariant variant = e.getValue();
                variant.setStockQuantity(Math.addExact(variant.getStockQuantity(), selectedVariants.get(e.getKey())));
                variants.save(variant);
            }
        }
        if (retailerId != null) {
            // Retailer rows are a separate inventory authority. Keep the item order
            // deterministic so concurrent multi-line returns cannot invert retailer-row locks.
            for (OrderItem item : orderItems) {
                int quantity = selected.isEmpty() ? item.getQuantity() : selected.getOrDefault(item.getId(), 0);
                if (quantity <= 0) continue;
                String sku = item.getVariantSku();
                if (sku == null || sku.isBlank()) {
                    var pp = products.findById(item.getProductId()).orElse(null);
                    sku = pp != null ? "SKU-" + pp.getSlug().toUpperCase() : "SKU-" + item.getProductId();
                }
                retailerInventory.restockStock(retailerId, item.getProductId(), item.getVariantId(), sku, quantity, order.getId(), actor, "Completed customer return");
            }
        }
        rr.markRestocked();
        if (retailerId != null) { settlements.markAdjustedForReturn(order.getId(), retailerId, rr.getRefundAmount(), actor); }
        rr.markSettlementAdjusted();
        ReturnRequest saved = returns.save(rr);
        notifications.refundUpdate(order.getCustomerId(), order.getId(), rr.getRefundAmount());
        return saved;
    }
}

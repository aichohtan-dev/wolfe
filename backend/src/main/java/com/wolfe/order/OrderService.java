package com.wolfe.order;

import com.wolfe.bundle.Bundle;
import com.wolfe.bundle.BundleItem;
import com.wolfe.bundle.BundleItemRepository;
import com.wolfe.bundle.BundleRepository;
import com.wolfe.catalog.Product;
import com.wolfe.catalog.ProductRepository;
import com.wolfe.catalog.ProductVariant;
import com.wolfe.catalog.ProductVariantRepository;
import com.wolfe.discount.Coupon;
import com.wolfe.discount.CouponRepository;
import com.wolfe.discount.CouponService;
import com.wolfe.inventory.Inventory;
import com.wolfe.inventory.InventoryRepository;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    private static final Set<String> TERMINAL = Set.of("DELIVERED", "CANCELLED");
    private final OrderRepository orders;
    private final OrderItemRepository items;
    private final ProductRepository products;
    private final ProductVariantRepository variants;
    private final InventoryRepository inventory;
    private final CouponService couponService;
    private final CouponRepository coupons;
    private final OrderStatusHistoryRepository history;
    private final com.wolfe.notification.NotificationService notifications;
    private final com.wolfe.experience.ConfigurationRepository configurations;
    private final BundleRepository bundles;
    private final BundleItemRepository bundleItems;

    private static final long FREE_SHIPPING_THRESHOLD = 250000; // ₹2,500 in paise
    private static final long STANDARD_SHIPPING_FEE = 19900; // ₹199 in paise

    public OrderService(OrderRepository orders, OrderItemRepository items, ProductRepository products,
                        ProductVariantRepository variants, InventoryRepository inventory,
                        CouponService couponService, CouponRepository coupons,
                        OrderStatusHistoryRepository history, com.wolfe.notification.NotificationService notifications,
                        com.wolfe.experience.ConfigurationRepository configurations, BundleRepository bundles,
                        BundleItemRepository bundleItems) {
        this.orders = orders;
        this.items = items;
        this.products = products;
        this.variants = variants;
        this.inventory = inventory;
        this.couponService = couponService;
        this.coupons = coupons;
        this.history = history;
        this.notifications = notifications;
        this.configurations = configurations;
        this.bundles = bundles;
        this.bundleItems = bundleItems;
    }

    public long couponDiscount(String code, long subtotal) {
        if (subtotal < 0) throw new IllegalArgumentException("subtotal cannot be negative");
        return couponService.discount(code, subtotal);
    }

    public long shippingFee(long subtotal, String method) {
        if (subtotal < 0) throw new IllegalArgumentException("subtotal cannot be negative");
        String normalized = method == null || method.isBlank() ? "STANDARD" : method.trim().toUpperCase();
        if (!"STANDARD".equals(normalized)) throw new IllegalArgumentException("unsupported shipping method: " + normalized);
        return subtotal >= FREE_SHIPPING_THRESHOLD ? 0 : STANDARD_SHIPPING_FEE;
    }

    @Transactional
    public Order create(CreateOrder r) {
        if (!"COD".equalsIgnoreCase(r.paymentMethod()))
            throw new IllegalArgumentException("Online payment is not enabled yet; select COD");
        if (r.items().isEmpty())
            throw new IllegalArgumentException("order must contain at least one item");
        if (r.customerId() == null || r.customerName() == null || r.customerName().isBlank() ||
            r.customerEmail() == null || r.customerEmail().isBlank() || r.phone() == null || r.phone().isBlank() ||
            r.address() == null || r.address().isBlank() || r.city() == null || r.city().isBlank() ||
            r.pincode() == null || r.pincode().isBlank())
            throw new IllegalArgumentException("complete customer and delivery details are required");

        record Line(String key, Product product, ProductVariant variant, int quantity,
                    com.wolfe.experience.ProductConfiguration cfg, Long bundleId, long gross, long discount) {}

        List<Line> lines = new ArrayList<>();
        Map<Long, Integer> inventoryQuantities = new LinkedHashMap<>();
        Map<Long, Product> resolved = new LinkedHashMap<>();
        Map<Long, Bundle> bundleMap = new HashMap<>();
        Map<Long, List<Line>> bundleLines = new HashMap<>();

        for (Item input : r.items()) {
            if (input == null || input.quantity() < 1 || input.quantity() > 100)
                throw new IllegalArgumentException("quantity must be between 1 and 100");
            if (input.slug() == null || input.slug().isBlank())
                throw new IllegalArgumentException("product slug is required");

            Product p = products.findBySlug(input.slug())
                    .orElseThrow(() -> new IllegalArgumentException("product not found: " + input.slug()));
            if (!p.isActive())
                throw new IllegalArgumentException("product is not available: " + input.slug());

            ProductVariant variant = null;
            if (input.variantId() != null) {
                variant = variants.findById(input.variantId())
                        .filter(ProductVariant::isActive)
                        .orElseThrow(() -> new IllegalArgumentException("variant not found or inactive: " + input.variantId()));
                if (!Objects.equals(variant.getProduct().getId(), p.getId())) {
                    throw new IllegalArgumentException("variant does not belong to product " + p.getSlug());
                }
            } else if (input.variantSku() != null && !input.variantSku().isBlank()) {
                variant = variants.findBySku(input.variantSku().trim()).filter(ProductVariant::isActive).orElse(null);
            }

            com.wolfe.experience.ProductConfiguration cfg = null;
            if (input.configurationToken() != null && !input.configurationToken().isBlank()) {
                cfg = configurations.findByShareToken(input.configurationToken())
                        .orElseThrow(() -> new IllegalArgumentException("configuration not found"));
                if (!Objects.equals(cfg.getProductId(), p.getId()))
                    throw new IllegalArgumentException("configuration does not belong to product");
            }

            long unit;
            if (cfg != null) {
                unit = Math.addExact(cfg.getBasePrice(), cfg.getAddonPrice());
            } else if (variant != null && variant.getPrice() != null) {
                unit = variant.getPrice().movePointRight(2).longValueExact();
            } else {
                unit = p.getPrice().movePointRight(2).longValueExact();
            }

            long gross = Math.multiplyExact(unit, input.quantity());

            Long bid = input.bundleId();
            if (bid != null) {
                Bundle b = bundles.findById(bid).filter(Bundle::isActive)
                        .orElseThrow(() -> new IllegalArgumentException("bundle not found or inactive"));
                boolean included = bundleItems.findByBundleId(bid).stream()
                        .anyMatch(x -> Objects.equals(x.getProductId(), p.getId()) && x.getQuantity() == 1);
                if (!included) throw new IllegalArgumentException("product quantity does not match bundle");
                bundleMap.put(bid, b);
            }

            String varKey = variant != null ? String.valueOf(variant.getId()) : "";
            String key = p.getId() + "|" + (bid == null ? "" : bid) + "|" +
                         (input.configurationToken() == null ? "" : input.configurationToken()) + "|" + varKey;

            Line line = new Line(key, p, variant, input.quantity(), cfg, bid, gross, 0);
            lines.add(line);
            resolved.put(p.getId(), p);
            inventoryQuantities.merge(p.getId(), input.quantity(), Integer::sum);
            if (bid != null) {
                bundleLines.computeIfAbsent(bid, k -> new ArrayList<>()).add(line);
            }
        }

        for (var e : bundleLines.entrySet()) {
            var expected = bundleItems.findByBundleId(e.getKey()).stream().map(BundleItem::getProductId).collect(java.util.stream.Collectors.toSet());
            var actual = e.getValue().stream().map(x -> x.product().getId()).collect(java.util.stream.Collectors.toSet());
            if (!expected.equals(actual) || e.getValue().stream().map(Line::quantity).distinct().count() != 1)
                throw new IllegalArgumentException("bundle items must be added as one complete set");
        }

        Map<String, Long> lineDiscounts = new HashMap<>();
        long subtotal = 0;
        for (Line line : lines) subtotal = Math.addExact(subtotal, line.gross());

        for (var e : bundleLines.entrySet()) {
            long gross = e.getValue().stream().mapToLong(Line::gross).sum();
            Bundle b = bundleMap.get(e.getKey());
            long discount;
            if ("FIXED".equals(b.getDiscountType())) {
                discount = Math.min(gross, Math.multiplyExact(b.getDiscountValue().movePointRight(2).longValueExact(),
                        e.getValue().stream().findFirst().map(Line::quantity).orElse(1)));
            } else {
                discount = Math.min(gross, BigDecimal.valueOf(gross).multiply(b.getDiscountValue())
                        .divide(BigDecimal.valueOf(100), 0, java.math.RoundingMode.HALF_UP).longValueExact());
            }
            long allocated = 0;
            for (int i = 0; i < e.getValue().size(); i++) {
                Line l = e.getValue().get(i);
                long d = i == e.getValue().size() - 1 ? discount - allocated : Math.min(l.gross(), Math.round((double) discount * l.gross() / gross));
                allocated += d;
                lineDiscounts.put(l.key(), d);
            }
        }

        subtotal = Math.subtractExact(subtotal, lineDiscounts.values().stream().mapToLong(Long::longValue).sum());
        Coupon coupon = couponService.requireForOrder(r.couponCode(), subtotal);
        long discount = coupon == null ? 0 : couponService.calculate(coupon, subtotal);
        long shippingFee = shippingFee(subtotal, r.shippingMethod());
        long total = Math.addExact(Math.subtractExact(subtotal, discount), shippingFee);

        String id = "WLF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Order order = orders.save(new Order(
                id, r.customerId(), "CONFIRMED", total, "INR", "COD",
                r.shippingMethod() == null ? "STANDARD" : r.shippingMethod().trim().toUpperCase(),
                r.customerName().trim(), r.customerEmail().trim().toLowerCase(), r.phone().trim(),
                r.address().trim(), r.city().trim(), r.pincode().trim()
        ));
        order.setAmounts(subtotal, shippingFee, discount, coupon == null ? null : coupon.getCode());
        order = orders.save(order);
        history.save(new OrderStatusHistory(order.getId(), "CONFIRMED", "Order created"));
        notifications.orderStatus(order.getCustomerId(), order.getId(), "CONFIRMED");
        if (coupon != null) {
            coupon.incrementUsage();
            coupons.save(coupon);
        }

        for (Long pid : inventoryQuantities.keySet()) {
            Inventory stock = inventory.findByProductIdForUpdate(pid)
                    .orElseThrow(() -> new IllegalArgumentException("inventory not configured for product: " + resolved.get(pid).getSlug()));
            stock.reserve(inventoryQuantities.get(pid));
            inventory.save(stock);
        }

        for (Line l : lines) {
            long bundleDiscount = lineDiscounts.getOrDefault(l.key(), 0L);
            long unitAfterDiscount = Math.max(0, (l.gross() - bundleDiscount) / l.quantity());

            Long varId = l.variant() != null ? l.variant().getId() : null;
            String varSku = l.variant() != null ? l.variant().getSku() : null;
            String varTitle = l.variant() != null ? l.variant().getTitle() : null;
            String varAttrs = l.variant() != null ? String.format("{\"color\":\"%s\",\"material\":\"%s\",\"size\":\"%s\",\"finish\":\"%s\",\"image\":\"%s\"}",
                    l.variant().getColor() != null ? l.variant().getColor() : "",
                    l.variant().getMaterial() != null ? l.variant().getMaterial() : "",
                    l.variant().getSize() != null ? l.variant().getSize() : "",
                    l.variant().getFinish() != null ? l.variant().getFinish() : "",
                    l.variant().getImageUrl() != null ? l.variant().getImageUrl() : "") : null;

            items.save(new OrderItem(
                    id, l.product().getId(), l.product().getName(), l.quantity(), unitAfterDiscount,
                    l.cfg() == null ? null : l.cfg().getShareToken(),
                    l.cfg() == null ? null : l.cfg().getConfigJson(),
                    l.bundleId(), bundleDiscount,
                    varId, varSku, varTitle, varAttrs
            ));
        }

        return order;
    }

    @Transactional
    public Order transition(String id, String requested) {
        Order order = orders.findById(id).orElseThrow(() -> new NoSuchElementException("Order not found"));
        String next = requested.trim().toUpperCase(), current = order.getStatus();
        if (TERMINAL.contains(current)) throw new IllegalArgumentException("order is already " + current);
        if (!isAllowed(current, next)) throw new IllegalArgumentException("invalid order transition: " + current + " -> " + next);

        List<OrderItem> orderItems = items.findByOrderId(id);
        for (OrderItem item : orderItems) {
            Inventory stock = inventory.findByProductIdForUpdate(item.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("inventory missing for product " + item.getProductId()));
            if ("CANCELLED".equals(next)) stock.release(item.getQuantity());
            else if ("DELIVERED".equals(next)) stock.fulfill(item.getQuantity());
            inventory.save(stock);
        }
        order.setStatus(next);
        Order saved = orders.save(order);
        history.save(new OrderStatusHistory(saved.getId(), next, null));
        notifications.orderStatus(saved.getCustomerId(), saved.getId(), next);
        return saved;
    }

    private boolean isAllowed(String current, String next) {
        return switch (current) {
            case "CONFIRMED" -> Set.of("PROCESSING", "CANCELLED").contains(next);
            case "PROCESSING" -> Set.of("SHIPPED", "CANCELLED").contains(next);
            case "SHIPPED" -> "DELIVERED".equals(next);
            default -> false;
        };
    }

    public record CreateOrder(
            Long customerId,
            String customerName,
            String customerEmail,
            String phone,
            String address,
            String city,
            String pincode,
            String paymentMethod,
            String shippingMethod,
            String couponCode,
            List<Item> items
    ) {}

    public record Item(
            String slug,
            int quantity,
            String configurationToken,
            Long bundleId,
            Long variantId,
            String variantSku
    ) {}
}

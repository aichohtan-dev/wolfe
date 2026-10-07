package com.wolfe.order;

import com.wolfe.bundle.Bundle;
import com.wolfe.bundle.BundleItem;
import com.wolfe.bundle.BundleItemRepository;
import com.wolfe.bundle.BundleRepository;
import com.wolfe.catalog.Product;
import com.wolfe.catalog.ProductRepository;
import com.wolfe.catalog.ProductVariant;
import com.wolfe.catalog.ProductVariantRepository;
import com.wolfe.customer.CustomerRepository;
import com.wolfe.discount.Coupon;
import com.wolfe.discount.CouponRepository;
import com.wolfe.discount.CouponService;
import com.wolfe.inventory.Inventory;
import com.wolfe.inventory.InventoryRepository;
import java.math.BigDecimal;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final com.wolfe.experience.ConfigurationService configurations;
    private final BundleRepository bundles;
    private final BundleItemRepository bundleItems;
    private final com.wolfe.retailer.RetailerAllocationService retailerAllocationService;
    private final CustomerRepository customers;
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.wolfe.ops.SchedulerLockService concurrencyLocks;
    private static final int MAX_ACTIVE_COD_ORDERS = 3;
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private static final long FREE_SHIPPING_THRESHOLD = 250000; // ₹2,500 in paise
    private static final long STANDARD_SHIPPING_FEE = 19900; // ₹199 in paise

    public OrderService(OrderRepository orders, OrderItemRepository items, ProductRepository products,
                        ProductVariantRepository variants, InventoryRepository inventory,
                        CouponService couponService, CouponRepository coupons,
                        OrderStatusHistoryRepository history, com.wolfe.notification.NotificationService notifications,
                        com.wolfe.experience.ConfigurationService configurations, BundleRepository bundles,
                        BundleItemRepository bundleItems, com.wolfe.visual.AccessoryOptionRepository accessoryRepository) {
        this(orders, items, products, variants, inventory, couponService, coupons, history, notifications, configurations, bundles, bundleItems, accessoryRepository, null, null);
    }

    public OrderService(OrderRepository orders, OrderItemRepository items, ProductRepository products,
                        ProductVariantRepository variants, InventoryRepository inventory,
                        CouponService couponService, CouponRepository coupons,
                        OrderStatusHistoryRepository history, com.wolfe.notification.NotificationService notifications,
                        com.wolfe.experience.ConfigurationService configurations, BundleRepository bundles,
                        BundleItemRepository bundleItems) {
        this(orders, items, products, variants, inventory, couponService, coupons, history, notifications, configurations, bundles, bundleItems, null, null, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public OrderService(OrderRepository orders, OrderItemRepository items, ProductRepository products,
                        ProductVariantRepository variants, InventoryRepository inventory,
                        CouponService couponService, CouponRepository coupons,
                        OrderStatusHistoryRepository history, com.wolfe.notification.NotificationService notifications,
                        com.wolfe.experience.ConfigurationService configurations, BundleRepository bundles,
                        BundleItemRepository bundleItems, com.wolfe.visual.AccessoryOptionRepository accessoryRepository,
                        com.wolfe.retailer.RetailerAllocationService retailerAllocationService,
                        CustomerRepository customers) {
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
        this.accessoryRepository = accessoryRepository;
        this.retailerAllocationService = retailerAllocationService;
        this.customers = customers;
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
        if (r.idempotencyKey() == null || r.idempotencyKey().isBlank() || r.idempotencyKey().length() > 100) throw new IllegalArgumentException("Idempotency-Key is required");
        if (!"COD".equalsIgnoreCase(r.paymentMethod()))
            throw new IllegalArgumentException("Online payment is not enabled yet; select COD");
        if (r.items().isEmpty())
            throw new IllegalArgumentException("order must contain at least one item");
        if (r.customerId() == null || r.customerName() == null || r.customerName().isBlank() ||
            r.customerEmail() == null || r.customerEmail().isBlank() || r.phone() == null || r.phone().isBlank() ||
            r.address() == null || r.address().isBlank() || r.city() == null || r.city().isBlank() ||
            r.pincode() == null || r.pincode().isBlank())
            throw new IllegalArgumentException("complete customer and delivery details are required");

        if (customers != null) {
            var customer = customers.findByIdForUpdate(r.customerId())
                    .orElseThrow(() -> new IllegalArgumentException("customer not found: " + r.customerId()));
            if (!customer.isEmailVerified()) throw new IllegalStateException("email verification is required before placing a COD order");
            String normalizedPhone = r.phone().trim();
            String normalizedAddress = r.address().trim().toLowerCase(java.util.Locale.ROOT);
            // Customer row locking serializes one customer's orders, but phone/address COD caps
            // span multiple customers. Serialize those shared business keys as well so two
            // different customers cannot both pass the count check against the same last slot.
            if (concurrencyLocks != null) {
                concurrencyLocks.lockForKey("COD_PHONE", normalizedPhone);
                concurrencyLocks.lockForKey("COD_ADDRESS", normalizedAddress);
            }
            var existing = orders.findByCustomerIdAndIdempotencyKey(r.customerId(), r.idempotencyKey().trim());
            if (existing.isPresent()) return existing.get();
            Set<String> activeStatuses = Set.of("CONFIRMED", "PROCESSING", "SHIPPED");
            long activeCodOrders = orders.countByCustomerIdAndPaymentMethodAndStatusIn(r.customerId(), "COD", activeStatuses);
            long phoneCodOrders = orders.countByPhoneAndPaymentMethodAndStatusIn(normalizedPhone, "COD", activeStatuses);
            long addressCodOrders = orders.countByAddressIgnoreCaseAndPaymentMethodAndStatusIn(normalizedAddress, "COD", activeStatuses);
            int maxPhone = Integer.parseInt(System.getenv().getOrDefault("WOLFE_MAX_ACTIVE_COD_ORDERS_PER_PHONE", "5"));
            int maxAddress = Integer.parseInt(System.getenv().getOrDefault("WOLFE_MAX_ACTIVE_COD_ORDERS_PER_ADDRESS", "5"));
            if (activeCodOrders >= MAX_ACTIVE_COD_ORDERS) throw new IllegalStateException("maximum active COD orders reached; complete or cancel an existing order first");
            if (phoneCodOrders >= maxPhone) throw new IllegalStateException("maximum active COD orders for this phone reached");
            if (addressCodOrders >= maxAddress) throw new IllegalStateException("maximum active COD orders for this address reached");
        }

        record Line(String key, Product product, ProductVariant variant, int quantity,
                    com.wolfe.experience.ProductConfiguration cfg, Long bundleId, long gross, long discount) {}

        Map<String, Line> mergedLines = new LinkedHashMap<>();
        Map<Long, Integer> inventoryQuantities = new TreeMap<>();
        Map<Long, Integer> variantQuantities = new TreeMap<>();
        Map<Long, Product> resolved = new LinkedHashMap<>();
        Map<Long, Bundle> bundleMap = new HashMap<>();

        for (Item input : r.items()) {
            if (input == null || input.quantity() < 1 || input.quantity() > 100)
                throw new IllegalArgumentException("quantity must be between 1 and 100");
            if (input.slug() == null || input.slug().isBlank())
                throw new IllegalArgumentException("product slug is required");

            Product p = products.findBySlugIgnoreCase(input.slug().trim())
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
                variant = variants.findBySku(input.variantSku().trim().toUpperCase(java.util.Locale.ROOT))
                        .filter(ProductVariant::isActive)
                        .orElseThrow(() -> new IllegalArgumentException("variant SKU not found or inactive: " + input.variantSku()));
                if (!Objects.equals(variant.getProduct().getId(), p.getId())) {
                    throw new IllegalArgumentException("variant SKU does not belong to product " + p.getSlug());
                }
            }
            if (input.variantId() != null && input.variantSku() != null && !input.variantSku().isBlank()
                    && variant != null && !input.variantSku().trim().equalsIgnoreCase(variant.getSku())) {
                throw new IllegalArgumentException("variant ID and SKU do not identify the same variant");
            }

            com.wolfe.experience.ProductConfiguration cfg = null;
            if (input.configurationToken() != null && !input.configurationToken().isBlank()) {
                cfg = configurations.resolveForOrder(input.configurationToken(), r.customerId(), p.getId());
            }

            long unit;
            if (cfg != null) {
                // Re-price configuration from current server-side catalog data. Stored configuration
                // prices are metadata only and are never authoritative for checkout.
                long currentBase = variant != null && variant.getPrice() != null
                        ? variant.getPrice().movePointRight(2).longValueExact()
                        : p.getPrice().movePointRight(2).longValueExact();
                long currentAddon = 0;
                if (cfg.getSelectedAccessoryId() != null) {
                    currentAddon = configurations.currentAddonPrice(cfg, p.getId());
                }
                unit = Math.addExact(currentBase, currentAddon);
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
                        .anyMatch(x -> Objects.equals(x.getProductId(), p.getId()) && x.getQuantity() >= 1);
                if (!included) throw new IllegalArgumentException("product is not part of the bundle");
                bundleMap.put(bid, b);
            }

            String varKey = variant != null ? String.valueOf(variant.getId()) : "";
            String key = p.getId() + "|" + (bid == null ? "" : bid) + "|" +
                         (input.configurationToken() == null ? "" : input.configurationToken()) + "|" + varKey;

            Line line = new Line(key, p, variant, input.quantity(), cfg, bid, gross, 0);
            mergedLines.merge(key, line, (a, b) -> {
                int mergedQuantity = Math.addExact(a.quantity(), b.quantity());
                if (mergedQuantity > 100) throw new IllegalArgumentException("quantity must be between 1 and 100 per order line");
                return new Line(key, a.product(), a.variant(), mergedQuantity, a.cfg(), a.bundleId(),
                        Math.addExact(a.gross(), b.gross()), 0);
            });
            resolved.put(p.getId(), p);
            if (variant != null) variantQuantities.merge(variant.getId(), input.quantity(), Math::addExact);
        }

        List<Line> lines = new ArrayList<>(mergedLines.values());
        Map<Long, List<Line>> rebuiltBundleLines = new HashMap<>();
        inventoryQuantities.clear();
        for (Line line : lines) {
            // Variant stock is authoritative when a variant is selected; product inventory is
            // authoritative only for products without variants. This prevents two stock authorities
            // from being decremented for the same variant line.
            if (line.variant() == null) {
                inventoryQuantities.merge(line.product().getId(), line.quantity(), Integer::sum);
            }
            if (line.bundleId() != null) rebuiltBundleLines.computeIfAbsent(line.bundleId(), k -> new ArrayList<>()).add(line);
        }
        Map<Long, Integer> bundleUnits = new HashMap<>();
        for (var e : rebuiltBundleLines.entrySet()) {
            var expected = bundleItems.findByBundleId(e.getKey()).stream()
                    .collect(java.util.stream.Collectors.toMap(BundleItem::getProductId, BundleItem::getQuantity, Integer::sum));
            var actual = e.getValue().stream()
                    .collect(java.util.stream.Collectors.groupingBy(x -> x.product().getId(), java.util.stream.Collectors.summingInt(Line::quantity)));
            int units = -1;
            for (var expectedEntry : expected.entrySet()) {
                int baseQuantity = expectedEntry.getValue();
                int actualQuantity = actual.getOrDefault(expectedEntry.getKey(), 0);
                if (baseQuantity < 1 || actualQuantity < 1 || actualQuantity % baseQuantity != 0) {
                    throw new IllegalArgumentException("bundle items and quantities must match whole bundle units");
                }
                int candidate = actualQuantity / baseQuantity;
                if (units < 0) units = candidate;
                else if (units != candidate) throw new IllegalArgumentException("all bundle items must use the same bundle quantity");
            }
            if (actual.size() != expected.size() || units < 1) {
                throw new IllegalArgumentException("bundle items and quantities must exactly match the configured bundle");
            }
            bundleUnits.put(e.getKey(), units);
        }

        Map<String, Long> lineDiscounts = new HashMap<>();
        long subtotal = 0;
        for (Line line : lines) subtotal = Math.addExact(subtotal, line.gross());

        for (var e : rebuiltBundleLines.entrySet()) {
            long gross = e.getValue().stream().mapToLong(Line::gross).sum();
            Bundle b = bundleMap.get(e.getKey());
            long discount;
            if ("FIXED".equals(b.getDiscountType())) {
                discount = Math.min(gross, Math.multiplyExact(b.getDiscountValue().movePointRight(2).longValueExact(),
                        bundleUnits.getOrDefault(e.getKey(), 1)));
            } else {
                discount = Math.min(gross, BigDecimal.valueOf(gross).multiply(b.getDiscountValue())
                        .divide(BigDecimal.valueOf(100), 0, java.math.RoundingMode.HALF_UP).longValueExact());
            }
            long allocated = 0;
            for (Line l : e.getValue()) {
                long d = Math.min(l.gross(), BigDecimal.valueOf(discount).multiply(BigDecimal.valueOf(l.gross()))
                        .divide(BigDecimal.valueOf(gross), 0, java.math.RoundingMode.DOWN).longValueExact());
                lineDiscounts.put(l.key(), d);
                allocated = Math.addExact(allocated, d);
            }
            if (allocated < discount) {
                long remaining = discount - allocated;
                for (Line l : e.getValue()) {
                    long room = l.gross() - lineDiscounts.getOrDefault(l.key(), 0L);
                    if (room <= 0) continue;
                    long add = Math.min(room, remaining);
                    lineDiscounts.merge(l.key(), add, Long::sum);
                    remaining -= add;
                    if (remaining == 0) break;
                }
            }
            long currentBundleAllocated = e.getValue().stream()
                    .mapToLong(l -> lineDiscounts.getOrDefault(l.key(), 0L))
                    .sum();
            if (currentBundleAllocated != discount
                    || e.getValue().stream().anyMatch(l -> {
                        long d = lineDiscounts.getOrDefault(l.key(), 0L);
                        return d < 0 || d > l.gross();
                    })) {
                throw new IllegalStateException("bundle discount allocation invariant failed");
            }
        }

        subtotal = Math.subtractExact(subtotal, lineDiscounts.values().stream().mapToLong(Long::longValue).sum());
        // CouponService locks the coupon row before counting this customer's prior uses,
        // serializing concurrent orders for the same coupon and making usage limits deterministic.
        Coupon coupon = couponService.requireForOrder(r.couponCode(), subtotal, r.customerId(), 0);
        long discount = coupon == null ? 0 : couponService.calculate(coupon, subtotal);
        Map<String, Long> couponLineDiscounts = new HashMap<>();
        long couponAllocated = 0;
        if (discount > 0 && subtotal > 0) {
            for (int i = 0; i < lines.size(); i++) {
                Line l = lines.get(i);
                long bundleDiscount = lineDiscounts.getOrDefault(l.key(), 0L);
                long lineBase = l.gross() - bundleDiscount;
                long d = i == lines.size() - 1 ? discount - couponAllocated
                        : BigDecimal.valueOf(discount).multiply(BigDecimal.valueOf(lineBase))
                            .divide(BigDecimal.valueOf(subtotal), 0, java.math.RoundingMode.DOWN).longValueExact();
                d = Math.min(Math.max(0, d), lineBase);
                couponAllocated += d;
                couponLineDiscounts.put(l.key(), d);
            }
            if (couponAllocated < discount) {
                for (Line l : lines) {
                    long room = (l.gross() - lineDiscounts.getOrDefault(l.key(), 0L)) - couponLineDiscounts.getOrDefault(l.key(), 0L);
                    if (room <= 0) continue;
                    long add = Math.min(room, discount - couponAllocated);
                    couponLineDiscounts.merge(l.key(), add, Long::sum);
                    couponAllocated += add;
                    if (couponAllocated == discount) break;
                }
            }
        }
        if (couponAllocated != discount) throw new IllegalStateException("coupon discount allocation mismatch");
        // Free-shipping eligibility is based on the final discounted merchandise
        // subtotal, including both bundle and coupon discounts. Using the pre-coupon
        // subtotal here could grant free shipping to an order whose final merchandise
        // subtotal is below the configured threshold.
        long discountedMerchandiseSubtotal = Math.subtractExact(subtotal, discount);
        long shippingFee = shippingFee(discountedMerchandiseSubtotal, r.shippingMethod());
        long total = Math.addExact(discountedMerchandiseSubtotal, shippingFee);

        String id = "WLF-" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
        Order order = orders.save(new Order(
                id, r.customerId(), "CONFIRMED", total, "INR", "COD",
                r.shippingMethod() == null ? "STANDARD" : r.shippingMethod().trim().toUpperCase(),
                r.customerName().trim(), r.customerEmail().trim().toLowerCase(), r.phone().trim(),
                r.address().trim(), r.city().trim(), r.pincode().trim()
        ));
        order.setCoordinates(r.latitude() == null ? null : java.math.BigDecimal.valueOf(r.latitude()),
                r.longitude() == null ? null : java.math.BigDecimal.valueOf(r.longitude()));
        order.setIdempotencyKey(r.idempotencyKey().trim());
        order.setAmounts(subtotal, shippingFee, discount, coupon == null ? null : coupon.getCode());
        order = orders.save(order);
        history.save(new OrderStatusHistory(order.getId(), "CONFIRMED", "Order created", "CUSTOMER:" + order.getCustomerId()));
        notifications.orderStatus(order.getCustomerId(), order.getId(), "CONFIRMED");
        if (coupon != null) {
            coupon.incrementUsage();
            coupons.save(coupon);
        }

        for (Long vid : variantQuantities.keySet()) {
            ProductVariant v = variants.findByIdForUpdate(vid).orElseThrow(() -> new IllegalArgumentException("variant not found: " + vid));
            int q = variantQuantities.get(vid);
            if (!v.isActive() || v.getStockQuantity() < q) throw new IllegalArgumentException("insufficient variant stock for " + v.getSku());
            v.setStockQuantity(v.getStockQuantity() - q);
            variants.save(v);
        }

        for (Long pid : inventoryQuantities.keySet()) {
            Inventory stock = inventory.findByProductIdForUpdate(pid)
                    .orElseThrow(() -> new IllegalArgumentException("inventory not configured for product: " + resolved.get(pid).getSlug()));
            stock.reserve(inventoryQuantities.get(pid));
            inventory.save(stock);
        }

        for (Line l : lines) {
            long bundleDiscount = lineDiscounts.getOrDefault(l.key(), 0L);
            long couponLineDiscount = couponLineDiscounts.getOrDefault(l.key(), 0L);
            long lineNetAmount = Math.subtractExact(Math.subtractExact(l.gross(), bundleDiscount), couponLineDiscount);
            long unitAfterDiscount = l.quantity() == 0 ? 0 : BigDecimal.valueOf(lineNetAmount)
                    .divide(BigDecimal.valueOf(l.quantity()), 0, java.math.RoundingMode.HALF_UP).longValueExact();

            Long varId = l.variant() != null ? l.variant().getId() : null;
            String varSku = l.variant() != null ? l.variant().getSku() : null;
            String varTitle = l.variant() != null ? l.variant().getTitle() : null;
            String varAttrs = null;
            if (l.variant() != null) {
                try {
                    varAttrs = JSON_MAPPER.writeValueAsString(Map.of(
                            "color", Optional.ofNullable(l.variant().getColor()).orElse(""),
                            "material", Optional.ofNullable(l.variant().getMaterial()).orElse(""),
                            "size", Optional.ofNullable(l.variant().getSize()).orElse(""),
                            "finish", Optional.ofNullable(l.variant().getFinish()).orElse(""),
                            "image", Optional.ofNullable(l.variant().getImageUrl()).orElse("")));
                } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
                    throw new IllegalStateException("Unable to serialize variant attributes", ex);
                }
            }

            items.save(new OrderItem(
                    id, l.product().getId(), l.product().getName(), l.quantity(), unitAfterDiscount,
                    l.cfg() == null ? null : l.cfg().getShareToken(),
                    l.cfg() == null ? null : l.cfg().getConfigJson(),
                    l.bundleId(), bundleDiscount, couponLineDiscount, lineNetAmount,
                    varId, varSku, varTitle, varAttrs
            ));
        }

        if (retailerAllocationService != null) {
            List<OrderItem> savedItems = items.findByOrderId(order.getId());
            boolean allocated = retailerAllocationService.allocateOrder(order, savedItems);
            if (allocated) {
                // Keep the central reservation until the retailer explicitly accepts the order.
                // This is the fallback stock authority while an ASSIGNED fulfillment can still be
                // rejected or expire. Acceptance transfers authority to retailer stock and releases
                // the central reservation exactly once.
                history.save(new OrderStatusHistory(order.getId(), "CONFIRMED", "Order assigned to retailer; central stock reservation retained until acceptance", "SYSTEM:RETAILER_ALLOCATION"));
            } else {
                history.save(new OrderStatusHistory(order.getId(), "CONFIRMED", "Order awaiting retailer allocation; central stock reservation retained", "SYSTEM:RETAILER_ALLOCATION"));
            }
        }

        return order;
    }

    @Transactional
    public Order transition(String id, String requested) {
        return transition(id, requested, "SYSTEM");
    }

    @Transactional
    public Order transition(String id, String requested, String actor) {
        Order order = orders.findByIdForUpdate(id).orElseThrow(() -> new NoSuchElementException("Order not found"));
        String next = requested.trim().toUpperCase(), current = order.getStatus();
        if (TERMINAL.contains(current)) throw new IllegalArgumentException("order is already " + current);
        if ("CANCELLED".equals(next) && retailerAllocationService != null && retailerAllocationService.isFulfillmentOutForDelivery(id)) {
            throw new IllegalStateException("Order cannot be cancelled after retailer dispatch");
        }
        if (!isAllowed(current, next)) throw new IllegalArgumentException("invalid order transition: " + current + " -> " + next);
        if (retailerAllocationService != null) {
            if ("PROCESSING".equals(next) && !retailerAllocationService.isFulfillmentAcceptedOrBeyond(id)) {
                throw new IllegalStateException("Order can be marked PROCESSING only after retailer accepts the fulfillment");
            }
            if ("SHIPPED".equals(next) && !retailerAllocationService.isFulfillmentOutForDelivery(id)) {
                throw new IllegalStateException("Order can be marked SHIPPED only after retailer dispatches the fulfillment");
            }
            if ("DELIVERED".equals(next) && !retailerAllocationService.isFulfillmentDelivered(id)) {
                throw new IllegalStateException("Order can be marked DELIVERED only after retailer fulfillment is completed");
            }
        }

        // Lock order: coupon first, then VARIANTS, then INVENTORY.
        // Order creation uses the same variant -> inventory order. Keeping the resource
        // order identical across create/cancel prevents a cross-transaction lock inversion.
        if ("CANCELLED".equals(next) && couponCodePresent(order)) {
            couponService.releaseUsage(order.getCouponCode());
        }

        List<OrderItem> orderItems = items.findByOrderId(id).stream()
                .sorted(java.util.Comparator.comparing(OrderItem::getProductId).thenComparing(i -> i.getVariantId() == null ? Long.MIN_VALUE : i.getVariantId()))
                .toList();
        boolean centralStockStillReserved = retailerAllocationService == null || retailerAllocationService.isGlobalStockStillReserved(id);
        if ("CANCELLED".equals(next) && centralStockStillReserved) {
            // Acquire all variant locks before any central inventory locks. The deterministic
            // TreeMap ordering also prevents two transactions from taking variant locks in
            // different orders when an order contains multiple variants.
            Map<Long, Integer> centralVariantQuantities = new TreeMap<>();
            for (OrderItem item : orderItems) {
                if (item.getVariantId() != null) centralVariantQuantities.merge(item.getVariantId(), item.getQuantity(), Integer::sum);
            }
            Map<Long, ProductVariant> lockedVariants = new LinkedHashMap<>();
            for (var e : centralVariantQuantities.entrySet()) {
                ProductVariant v = variants.findByIdForUpdate(e.getKey()).orElseThrow(() -> new IllegalStateException("variant missing for order item"));
                lockedVariants.put(e.getKey(), v);
            }

            Map<Long, Integer> centralProductQuantities = new TreeMap<>();
            for (OrderItem item : orderItems) {
                if (item.getVariantId() == null) centralProductQuantities.merge(item.getProductId(), item.getQuantity(), Integer::sum);
            }
            for (var e : centralProductQuantities.entrySet()) {
                Inventory stock = inventory.findByProductIdForUpdate(e.getKey())
                        .orElseThrow(() -> new IllegalArgumentException("inventory missing for product " + e.getKey()));
                stock.release(e.getValue());
                inventory.save(stock);
            }
            for (var e : lockedVariants.entrySet()) {
                ProductVariant v = e.getValue();
                v.setStockQuantity(Math.addExact(v.getStockQuantity(), centralVariantQuantities.get(e.getKey())));
                variants.save(v);
            }
        } else if ("DELIVERED".equals(next) && centralStockStillReserved) {
            Map<Long, Integer> centralProductQuantities = new TreeMap<>();
            for (OrderItem item : orderItems) if (item.getVariantId() == null) centralProductQuantities.merge(item.getProductId(), item.getQuantity(), Integer::sum);
            for (var e : centralProductQuantities.entrySet()) {
                Inventory stock = inventory.findByProductIdForUpdate(e.getKey())
                        .orElseThrow(() -> new IllegalArgumentException("inventory missing for product " + e.getKey()));
                stock.fulfill(e.getValue());
                inventory.save(stock);
            }
        }

        order.setStatus(next);
        Order saved = orders.save(order);

        if ("CANCELLED".equals(next)) {
            if (retailerAllocationService != null) retailerAllocationService.cancelAllocation(id);
        }
        history.save(new OrderStatusHistory(saved.getId(), next, null, actor));
        notifications.orderStatus(saved.getCustomerId(), saved.getId(), next);
        return saved;
    }

    private boolean couponCodePresent(Order order) {
        return order.getCouponCode() != null && !order.getCouponCode().isBlank();
    }

    private com.wolfe.visual.AccessoryOption bundleAccessory(Long accessoryId, Long productId) {
        var accessory = accessoryRepository.findById(accessoryId)
                .filter(com.wolfe.visual.AccessoryOption::isActive)
                .orElseThrow(() -> new IllegalArgumentException("configuration accessory is no longer available"));
        if (!Objects.equals(accessory.getProduct().getId(), productId))
            throw new IllegalArgumentException("configuration accessory does not belong to product");
        return accessory;
    }

    private final com.wolfe.visual.AccessoryOptionRepository accessoryRepository;

    private boolean isAllowed(String current, String next) {
        return switch (current) {
            case "CONFIRMED" -> Set.of("PROCESSING", "CANCELLED").contains(next);
            case "PROCESSING" -> Set.of("SHIPPED", "CANCELLED").contains(next);
            case "SHIPPED" -> "DELIVERED".equals(next);
            default -> false;
        };
    }

    public record CreateOrder(
            Long customerId, String customerName, String customerEmail, String phone, String address, String city,
            String pincode, Double latitude, Double longitude, String paymentMethod, String shippingMethod, String couponCode, List<Item> items, String idempotencyKey
    ) {
        public CreateOrder(Long customerId, String customerName, String customerEmail, String phone, String address, String city,
                           String pincode, String paymentMethod, String shippingMethod, String couponCode, List<Item> items) {
            this(customerId, customerName, customerEmail, phone, address, city, pincode, null, null, paymentMethod, shippingMethod, couponCode, items, java.util.UUID.randomUUID().toString());
        }
    }

    public record Item(
            String slug,
            int quantity,
            String configurationToken,
            Long bundleId,
            Long variantId,
            String variantSku
    ) {}
}

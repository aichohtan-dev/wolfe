package com.wolfe.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import static com.wolfe.security.CustomerAccess.requireCustomer;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderRepository orders;
    private final OrderStatusHistoryRepository history;
    private final OrderItemRepository items;
    private final com.wolfe.catalog.ProductRepository products;
    private final OrderService service;
    private final com.wolfe.security.RateLimitService rateLimits;

    public OrderController(OrderRepository orders, OrderItemRepository items, com.wolfe.catalog.ProductRepository products, OrderService service,
                           OrderStatusHistoryRepository history) {
        this(orders, items, products, service, history, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public OrderController(OrderRepository orders, OrderItemRepository items, com.wolfe.catalog.ProductRepository products, OrderService service,
                           OrderStatusHistoryRepository history, com.wolfe.security.RateLimitService rateLimits) {
        this.orders = orders;
        this.items = items;
        this.products = products;
        this.service = service;
        this.history = history;
        this.rateLimits = rateLimits;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderView create(@Valid @RequestBody CreateOrder r, @RequestHeader("Idempotency-Key") String idempotencyKey, Authentication auth) {
        requireCustomer(auth, r.customerId());
        return new OrderView(service.create(new OrderService.CreateOrder(
                r.customerId(), r.customerName(), r.customerEmail(), r.phone(), r.address(), r.city(), r.pincode(),
                r.latitude(), r.longitude(), r.paymentMethod(), r.shippingMethod(), r.couponCode(),
                r.items().stream().map(i -> new OrderService.Item(
                        i.slug(), i.quantity(), i.configurationToken(), i.bundleId(), i.variantId(), i.variantSku()
                )).toList(), idempotencyKey
        )));
    }

    @GetMapping("/shipping-quote")
    public Map<String, Object> shippingQuote(@RequestParam long subtotal,
                                            @RequestParam(defaultValue = "STANDARD") String shippingMethod) {
        long fee = service.shippingFee(subtotal, shippingMethod);
        return Map.of("subtotal", subtotal, "shippingFee", fee, "total", Math.addExact(subtotal, fee), "shippingMethod", shippingMethod.trim().toUpperCase(), "thresholdBasis", "POST_DISCOUNT_SUBTOTAL", "thresholdPolicy", "Free STANDARD shipping applies when final discounted merchandise subtotal is at least ₹2,500.");
    }

    @GetMapping("/coupon-quote")
    public Map<String, Object> couponQuote(@RequestParam long subtotal, @RequestParam String couponCode, HttpServletRequest request) {
        if (rateLimits != null) rateLimits.check("coupon-quote", "public", clientIp(request));
        try {
            long discount = service.couponDiscount(couponCode, subtotal);
            return Map.of("subtotal", subtotal, "discount", discount, "couponCode", couponCode.trim().toUpperCase(), "payableSubtotal", Math.subtractExact(subtotal, discount), "availability", "INFORMATIONAL", "checkoutRevalidates", true, "consumption", "Coupon usage is consumed only by a successful order checkout.");
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("INVALID_COUPON");
        }
    }

    private String clientIp(HttpServletRequest request) { return request.getRemoteAddr(); }

    @GetMapping("/customer/{customerId}")
    public List<OrderView> byCustomer(@PathVariable Long customerId, Authentication auth) {
        requireCustomer(auth, customerId);
        return orders.findByCustomerIdOrderByCreatedAtDesc(customerId).stream().map(OrderView::new).toList();
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable String id, Authentication auth) {
        var order = orders.findById(id).orElseThrow(() -> new NoSuchElementException("Order not found"));
        requireCustomer(auth, order.getCustomerId());
        return Map.of("order", new OrderView(order), "items", items.findByOrderId(id).stream().map(OrderItemView::new).toList());
    }

    @GetMapping("/{id}/history")
    public List<OrderStatusHistory> history(@PathVariable String id, Authentication auth) {
        var order = orders.findById(id).orElseThrow(() -> new NoSuchElementException("Order not found"));
        requireCustomer(auth, order.getCustomerId());
        return history.findByOrderIdOrderByCreatedAtAsc(id);
    }

    @PostMapping("/{id}/cancel")
    public OrderView cancel(@PathVariable String id, Authentication auth) {
        var order = orders.findById(id).orElseThrow(() -> new NoSuchElementException("Order not found"));
        requireCustomer(auth, order.getCustomerId());
        return new OrderView(service.transition(id, "CANCELLED", "CUSTOMER:" + auth.getDetails()));
    }

    public record CreateOrder(
            @NotNull Long customerId,
            @NotBlank @Size(max = 200) String customerName,
            @Email @NotBlank @Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[A-Za-z]{2,63}$") @Size(max = 320) String customerEmail,
            @NotBlank @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$") String phone,
            @NotBlank @Size(max = 1000) String address,
            @NotBlank @Size(max = 120) String city,
            @NotBlank @Pattern(regexp = "^[1-9]\\d{5}$") String pincode,
            @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
            @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
            @NotBlank @Size(max = 40) String paymentMethod,
            @NotBlank @Size(max = 60) String shippingMethod,
            @Size(max=40) String couponCode,
            @NotEmpty @Size(max=50) List<Item> items
    ) {}

    public record OrderView(String id, String supportNumber, Long customerId, String status, long total, long subtotal, long shippingFee,
                            long discountAmount, String couponCode, String currency, String paymentMethod, String shippingMethod,
                            String customerName, String customerEmail, String phone, String address, String city, String pincode,
                            java.math.BigDecimal latitude, java.math.BigDecimal longitude, java.time.Instant createdAt) {
        public OrderView(Order o) {
            this(o.getId(), o.getSupportNumber(), o.getCustomerId(), o.getStatus(), o.getTotal(), o.getSubtotal(), o.getShippingFee(),
                 o.getDiscountAmount(), o.getCouponCode(), o.getCurrency(), o.getPaymentMethod(), o.getShippingMethod(),
                 o.getCustomerName(), o.getCustomerEmail(), o.getPhone(), o.getAddress(), o.getCity(), o.getPincode(),
                 o.getLatitude(), o.getLongitude(), o.getCreatedAt());
        }
    }
    public record OrderItemView(Long id, Long productId, String productName, int quantity, long unitPrice, Long bundleId,
                                long bundleDiscount, long couponDiscount, long lineNetAmount, Long variantId, String variantSku, String variantTitle) {
        public OrderItemView(OrderItem i) {
            this(i.getId(), i.getProductId(), i.getProductName(), i.getQuantity(), i.getUnitPrice(), i.getBundleId(),
                 i.getBundleDiscount(), i.getCouponDiscount(), i.getLineNetAmount(), i.getVariantId(), i.getVariantSku(), i.getVariantTitle());
        }
    }

    public record Item(
            @NotBlank @Size(max = 200) String slug,
            @Positive int quantity,
            @Size(max=32) String configurationToken,
            @Positive Long bundleId,
            @Positive Long variantId,
            @Size(max=120) String variantSku
    ) {}
}

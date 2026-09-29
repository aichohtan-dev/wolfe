package com.wolfe.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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

    public OrderController(OrderRepository orders, OrderItemRepository items, com.wolfe.catalog.ProductRepository products, OrderService service,
                           OrderStatusHistoryRepository history) {
        this.orders = orders;
        this.items = items;
        this.products = products;
        this.service = service;
        this.history = history;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order create(@Valid @RequestBody CreateOrder r, Authentication auth) {
        requireCustomer(auth, r.customerId());
        return service.create(new OrderService.CreateOrder(
                r.customerId(), r.customerName(), r.customerEmail(), r.phone(), r.address(), r.city(), r.pincode(),
                r.paymentMethod(), r.shippingMethod(), r.couponCode(),
                r.items().stream().map(i -> new OrderService.Item(
                        i.slug(), i.quantity(), i.configurationToken(), i.bundleId(), i.variantId(), i.variantSku()
                )).toList()
        ));
    }

    @GetMapping("/shipping-quote")
    public Map<String, Object> shippingQuote(@RequestParam long subtotal,
                                            @RequestParam(defaultValue = "STANDARD") String shippingMethod) {
        long fee = service.shippingFee(subtotal, shippingMethod);
        return Map.of("subtotal", subtotal, "shippingFee", fee, "total", Math.addExact(subtotal, fee), "shippingMethod", shippingMethod.trim().toUpperCase());
    }

    @GetMapping("/coupon-quote")
    public Map<String, Object> couponQuote(@RequestParam long subtotal, @RequestParam String couponCode) {
        long discount = service.couponDiscount(couponCode, subtotal);
        return Map.of("subtotal", subtotal, "discount", discount, "couponCode", couponCode.trim().toUpperCase(), "payableSubtotal", Math.subtractExact(subtotal, discount));
    }

    @GetMapping("/customer/{customerId}")
    public List<Order> byCustomer(@PathVariable Long customerId, Authentication auth) {
        requireCustomer(auth, customerId);
        return orders.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable String id, Authentication auth) {
        var order = orders.findById(id).orElseThrow(() -> new NoSuchElementException("Order not found"));
        requireCustomer(auth, order.getCustomerId());
        return Map.of("order", order, "items", items.findByOrderId(id));
    }

    @GetMapping("/{id}/history")
    public List<OrderStatusHistory> history(@PathVariable String id, Authentication auth) {
        var order = orders.findById(id).orElseThrow(() -> new NoSuchElementException("Order not found"));
        requireCustomer(auth, order.getCustomerId());
        return history.findByOrderIdOrderByCreatedAtAsc(id);
    }

    @PostMapping("/{id}/cancel")
    public Order cancel(@PathVariable String id, Authentication auth) {
        var order = orders.findById(id).orElseThrow(() -> new NoSuchElementException("Order not found"));
        requireCustomer(auth, order.getCustomerId());
        return service.transition(id, "CANCELLED");
    }

    public record CreateOrder(
            @NotNull Long customerId,
            @NotBlank String customerName,
            @Email @NotBlank String customerEmail,
            @NotBlank @Size(min = 7, max = 30) String phone,
            @NotBlank @Size(max = 1000) String address,
            @NotBlank String city,
            @NotBlank @Size(min = 4, max = 20) String pincode,
            @NotBlank String paymentMethod,
            @NotBlank String shippingMethod,
            String couponCode,
            @NotEmpty List<Item> items
    ) {}

    public record Item(
            @NotBlank String slug,
            @Positive int quantity,
            String configurationToken,
            Long bundleId,
            Long variantId,
            String variantSku
    ) {}
}

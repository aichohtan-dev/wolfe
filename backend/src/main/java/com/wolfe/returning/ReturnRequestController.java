package com.wolfe.returning;

import com.wolfe.order.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import static com.wolfe.security.CustomerAccess.requireCustomer;

@RestController
@RequestMapping("/api/v1/customers/{customerId}/returns")
public class ReturnRequestController {
    private final ReturnRequestRepository returns;
    private final OrderRepository orders;
    public ReturnRequestController(ReturnRequestRepository returns, OrderRepository orders) {
        this.returns = returns;
        this.orders = orders;
    }
    public record CreateReturn(@NotBlank @Size(max = 1000) String reason) {
    }
    @GetMapping public List<ReturnRequest> list(@PathVariable Long customerId, Authentication auth) {
        requireCustomer(auth, customerId);
        return returns.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }
    @PostMapping("/{orderId}")
    @ResponseStatus(HttpStatus.CREATED) public ReturnRequest create(@PathVariable Long customerId, @PathVariable String orderId,
    @Valid @RequestBody CreateReturn r,
    Authentication auth) {
        requireCustomer(auth, customerId);
        Order o = orders.findById(orderId).orElseThrow(() -> new NoSuchElementException("Order not found"));
        if (!o.getCustomerId().equals(customerId))throw new org.springframework.security.access.AccessDeniedException("Order access denied");
        if (!"DELIVERED".equals(o.getStatus()))throw new IllegalArgumentException("Returns can be requested after delivery");
        if (returns.findByOrderId(orderId).isPresent())throw new IllegalArgumentException("A return request already exists for this order");
        return returns.save(new ReturnRequest(orderId, customerId, r.reason()));
    }
}

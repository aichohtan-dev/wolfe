package com.wolfe.review;

import com.wolfe.catalog.ProductRepository;
import com.wolfe.order.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews") public class ProductReviewController {
    private final ProductReviewRepository reviews;
    private final ProductRepository products;
    private final OrderRepository orders;
    private final OrderItemRepository items;
    public ProductReviewController(ProductReviewRepository r, ProductRepository p, OrderRepository o, OrderItemRepository i) {
        reviews = r;
        products = p;
        orders = o;
        items = i;
    }
    @GetMapping("/product/{slug}") public Map<String, Object> product(@PathVariable String slug) {
        var p = products.findBySlug(slug).filter(x -> x.isActive()).orElseThrow(() -> new NoSuchElementException("Product not found"));
        var rows = reviews.findByProductIdAndStatusOrderByCreatedAtDesc(p.getId(), "APPROVED");
        double avg = rows.stream().mapToInt(ProductReview::getRating).average().orElse(0);
        return Map.of("average", avg, "count", rows.size(), "reviews", rows.stream().map(r -> Map.of("id", r.getId(), "rating", r.getRating(), "review",
        r.getReview(), "createdAt",
        r.getCreatedAt())).toList());
    }
    public record CreateReview(@Min(1) @Max(5) int rating, @NotBlank @Size(max = 1000) String review) {
    }
    @PostMapping("/product/{slug}") public ProductReview create(@PathVariable String slug, @Valid @RequestBody CreateReview r, Authentication auth) {
        Long cid = (Long)auth.getDetails();
        var p = products.findBySlug(slug).filter(x -> x.isActive()).orElseThrow();
        boolean purchased = orders.findByCustomerIdOrderByCreatedAtDesc(cid).stream().map(Order::getId).flatMap(id -> items.findByOrderId(id).stream()).anyMatch(i -> Objects.equals(i.getProductId(),
        p.getId()));
        if (!purchased)throw new IllegalArgumentException("Purchase required before reviewing");
        if (reviews.findByProductIdAndCustomerId(p.getId(), cid).isPresent())throw new IllegalArgumentException("Review already exists");
        return reviews.save(new ProductReview(p.getId(), cid, r.rating(), r.review().trim()));
    }
}

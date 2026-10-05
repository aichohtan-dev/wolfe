package com.wolfe.review;

import com.wolfe.catalog.ProductRepository;
import com.wolfe.order.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import static com.wolfe.security.CustomerAccess.requireCustomer;

@RestController
@RequestMapping("/api/v1/reviews") public class ProductReviewController {
    private final ProductReviewRepository reviews;
    private final ProductRepository products;
    private final OrderRepository orders;
    private final com.wolfe.security.RateLimitService rateLimits;
    public ProductReviewController(ProductReviewRepository r, ProductRepository p, OrderRepository o, OrderItemRepository i, com.wolfe.security.RateLimitService rateLimits) {
        reviews = r;
        products = p;
        orders = o;
        this.rateLimits = rateLimits;
    }
    @GetMapping("/product/{slug}") public Map<String, Object> product(@PathVariable String slug) {
        var p = products.findBySlugIgnoreCase(slug.trim()).filter(x -> x.isActive()).orElseThrow(() -> new NoSuchElementException("Product not found"));
        return productPage(slug, 0, 20);
    }
    @GetMapping("/product/{slug}/page") public Map<String, Object> productPage(@PathVariable String slug, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int pageSize) {
        var p = products.findBySlugIgnoreCase(slug.trim()).filter(ProductReviewController::activeProduct).orElseThrow(() -> new NoSuchElementException("Product not found"));
        int size = Math.min(Math.max(1, pageSize), 50);
        var rows = reviews.findByProductIdAndStatus(p.getId(), "APPROVED", PageRequest.of(Math.max(0, page), size, Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"))));
        double avg = Math.round(reviews.averageRating(p.getId(), "APPROVED") * 100.0) / 100.0;
        return Map.of("average", avg, "count", rows.getTotalElements(), "page", rows.getNumber(), "pageSize", rows.getSize(), "totalPages", rows.getTotalPages(), "reviews", rows.getContent().stream().map(r -> Map.of("id", r.getId(), "rating", r.getRating(), "review", r.getReview(), "createdAt", r.getCreatedAt())).toList());
    }
    private static boolean activeProduct(com.wolfe.catalog.Product p) { return p.isActive(); }

    public record CreateReview(@Min(1) @Max(5) int rating, @NotBlank @Size(max = 1000) String review) {
    }
    public record ReviewView(Long id, int rating, String review, String status, java.time.Instant createdAt) {
        ReviewView(ProductReview r) { this(r.getId(), r.getRating(), r.getReview(), r.getStatus(), r.getCreatedAt()); }
    }
    @PutMapping("/{id}") public ReviewView update(@PathVariable Long id, @Valid @RequestBody CreateReview r, Authentication auth, HttpServletRequest request) {
        Long cid=(Long)auth.getDetails();
        rateLimits.check("review-edit", String.valueOf(cid), request.getRemoteAddr());
        var review=reviews.findById(id).orElseThrow(() -> new NoSuchElementException("Review not found"));
        if(!Objects.equals(review.getCustomerId(),cid)) throw new org.springframework.security.access.AccessDeniedException("Review access denied");
        review.updateContent(r.rating(),r.review().trim());
        return new ReviewView(reviews.save(review));
    }
    @DeleteMapping("/{id}") @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id, Authentication auth, HttpServletRequest request) {
        Long cid=(Long)auth.getDetails();
        rateLimits.check("review-delete", String.valueOf(cid), request.getRemoteAddr()); var review=reviews.findById(id).orElseThrow(() -> new NoSuchElementException("Review not found"));
        if(!Objects.equals(review.getCustomerId(),cid)) throw new org.springframework.security.access.AccessDeniedException("Review access denied");
        reviews.delete(review);
    }

    @GetMapping("/product/{slug}/mine") public ReviewView mine(@PathVariable String slug, Authentication auth) {
        Long cid = requireCustomer(auth);
        var p = products.findBySlugIgnoreCase(slug.trim()).filter(ProductReviewController::activeProduct)
                .orElseThrow(() -> new NoSuchElementException("Product not found"));
        return reviews.findByProductIdAndCustomerId(p.getId(), cid)
                .map(ReviewView::new)
                .orElseThrow(() -> new NoSuchElementException("Review not found"));
    }

    @PostMapping("/product/{slug}") public ReviewView create(@PathVariable String slug, @Valid @RequestBody CreateReview r, Authentication auth, HttpServletRequest request) {
        if (auth == null || !(auth.getDetails() instanceof Long cid)) throw new org.springframework.security.access.AccessDeniedException("Customer authentication required");
        rateLimits.check("review", String.valueOf(cid), request.getRemoteAddr());
        var p = products.findBySlugIgnoreCase(slug.trim()).filter(x -> x.isActive()).orElseThrow(() -> new NoSuchElementException("Product not found"));
        boolean purchased = orders.existsDeliveredProductForCustomer(cid, p.getId());
        if (!purchased)throw new IllegalArgumentException("Purchase required before reviewing");
        if (reviews.findByProductIdAndCustomerId(p.getId(), cid).isPresent())throw new IllegalArgumentException("Review already exists");
        return new ReviewView(reviews.save(new ProductReview(p.getId(), cid, r.rating(), r.review().trim())));
    }
}

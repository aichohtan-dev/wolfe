package com.wolfe.review;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "product_reviews", uniqueConstraints = @UniqueConstraint(name = "uk_review_customer_product", columnNames = {
    "customer_id", "product_id"
}
))
public class ProductReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    @Column(name = "product_id", nullable = false) Long productId;
    @Column(name = "customer_id", nullable = false) Long customerId;
    @Column(nullable = false) int rating;
    @Column(nullable = false, length = 1000) String review;
    @Column(nullable = false) String status = "PENDING";
    @Column(name = "created_at", nullable = false) Instant createdAt = Instant.now();
    protected ProductReview() {
    }
    public ProductReview(Long p, Long c, int r, String t) {
        productId = p;
        customerId = c;
        rating = r;
        review = t;
    }
    public Long getId() {
        return id;
    }
    public Long getProductId() {
        return productId;
    }
    public Long getCustomerId() {
        return customerId;
    }
    public int getRating() {
        return rating;
    }
    public String getReview() {
        return review;
    }
    public String getStatus() {
        return status;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public void moderate(String s) {
        status = s;
    }
}

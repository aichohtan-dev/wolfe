package com.wolfe.experience;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "recently_viewed", uniqueConstraints = @UniqueConstraint(columnNames = {
    "customer_id", "product_id"
}
))
public class RecentlyViewed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "customer_id", nullable = false) private Long customerId;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(nullable = false) private Instant viewedAt = Instant.now();
    protected RecentlyViewed() {
    }
    public RecentlyViewed(Long customerId, Long productId) {
        this.customerId = customerId;
        this.productId = productId;
    }
    public Long getId() {
        return id;
    }
    public Long getCustomerId() {
        return customerId;
    }
    public Long getProductId() {
        return productId;
    }
    public Instant getViewedAt() {
        return viewedAt;
    }
    public void touch() {
        viewedAt = Instant.now();
    }
}

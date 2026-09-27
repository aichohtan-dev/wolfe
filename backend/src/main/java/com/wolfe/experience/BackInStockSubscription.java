package com.wolfe.experience;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "back_in_stock_subscriptions", uniqueConstraints = @UniqueConstraint(columnNames = {
    "customer_id", "product_id"
}
))
public class BackInStockSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "customer_id", nullable = false) private Long customerId;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(nullable = false) private boolean active = true;
    @Column(nullable = false) private Instant createdAt = Instant.now();
    protected BackInStockSubscription() {
    }
    public BackInStockSubscription(Long customerId, Long productId) {
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
    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
}

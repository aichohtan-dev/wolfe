package com.wolfe.quote;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "quote_requests") public class QuoteRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) Long id; @Version long version;
    @Column(name = "customer_id", nullable = false) Long customerId;
    @Column(name = "product_id") Long productId;
    @Column(name = "configuration_id") Long configurationId;
    @Column(nullable = false, length = 1000) String message;
    @Column(nullable = false) String status = "NEW";
    @Column(name = "created_at", nullable = false) Instant createdAt = Instant.now();
    protected QuoteRequest() {
    }
    public QuoteRequest(Long c, Long p, String m) {
        customerId = c;
        productId = p;
        message = m;
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
    public Long getConfigurationId() {
        return configurationId;
    }
    public void setConfigurationId(Long id) {
        this.configurationId = id;
    }
    public String getMessage() {
        return message;
    }
    public String getStatus() {
        return status;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public void status(String s) {
        status = s;
    }
}

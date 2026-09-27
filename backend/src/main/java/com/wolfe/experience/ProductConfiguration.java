package com.wolfe.experience;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "product_configurations")
public class ProductConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 32) private String shareToken;
    @Column(name = "customer_id") private Long customerId;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(name = "selected_accessory_id") private Long selectedAccessoryId;
    @Column(name = "addon_price", nullable = false) private long addonPrice;
    @Column(name = "base_price", nullable = false) private long basePrice;
    @Column(nullable = false, length = 12000) private String configJson;
    @Column(nullable = false) private Instant createdAt = Instant.now();
    protected ProductConfiguration() {
    }
    public ProductConfiguration(String token, Long customerId, Long productId, Long selectedAccessoryId, long addonPrice, long basePrice, String configJson) {
        this.shareToken = token;
        this.customerId = customerId;
        this.productId = productId;
        this.selectedAccessoryId = selectedAccessoryId;
        this.addonPrice = addonPrice;
        this.basePrice = basePrice;
        this.configJson = configJson;
    }
    public Long getId() {
        return id;
    }
    public String getShareToken() {
        return shareToken;
    }
    public Long getCustomerId() {
        return customerId;
    }
    public Long getProductId() {
        return productId;
    }
    public Long getSelectedAccessoryId() {
        return selectedAccessoryId;
    }
    public long getAddonPrice() {
        return addonPrice;
    }
    public long getBasePrice() {
        return basePrice;
    }
    public String getConfigJson() {
        return configJson;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
}

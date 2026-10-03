package com.wolfe.retailer;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "retailer_margin_rules")
public class RetailerMarginRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "retailer_id")
    private Long retailerId;

    @Column(length = 120)
    private String category;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "variant_id")
    private Long variantId;

    @Column(name = "margin_type", nullable = false, length = 20)
    private String marginType = "PERCENTAGE"; // PERCENTAGE, FIXED

    @Column(name = "margin_value", nullable = false)
    private BigDecimal marginValue = new BigDecimal("10.0");

    @Column(nullable = false)
    private int priority = 0;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public RetailerMarginRule() {}

    public RetailerMarginRule(Long retailerId, String category, Long productId, Long variantId, String marginType, BigDecimal marginValue, int priority) {
        this.retailerId = retailerId;
        this.category = category;
        this.productId = productId;
        this.variantId = variantId;
        this.marginType = marginType != null ? marginType.trim().toUpperCase() : "PERCENTAGE";
        this.marginValue = marginValue != null ? marginValue : new BigDecimal("10.0");
        if (!"PERCENTAGE".equals(this.marginType) && !"FIXED".equals(this.marginType)) throw new IllegalArgumentException("invalid margin type");
        if (this.marginValue.signum() <= 0 || ("PERCENTAGE".equals(this.marginType) && this.marginValue.compareTo(new BigDecimal("100")) > 0)) throw new IllegalArgumentException("invalid margin value");
        this.priority = priority;
        this.active = true;
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public Long getRetailerId() { return retailerId; }
    public void setRetailerId(Long retailerId) { this.retailerId = retailerId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getVariantId() { return variantId; }
    public void setVariantId(Long variantId) { this.variantId = variantId; }
    public String getMarginType() { return marginType; }
    public void setMarginType(String marginType) { this.marginType = marginType; }
    public BigDecimal getMarginValue() { return marginValue; }
    public void setMarginValue(BigDecimal marginValue) { this.marginValue = marginValue; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}

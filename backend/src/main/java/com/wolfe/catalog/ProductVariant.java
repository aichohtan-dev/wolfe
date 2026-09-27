package com.wolfe.catalog;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "product_variants", indexes = {
    @Index(name = "idx_product_variants_product", columnList = "product_id")
}
)
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false) private Product product;
    @Column(nullable = false, length = 120) private String optionName;
    @Column(nullable = false, length = 180) private String optionValue;
    @Column(length = 120, unique = true) private String sku;
    @Column(precision = 12, scale = 2) private BigDecimal priceOverride;
    @Column(nullable = false) private boolean active = true;
    protected ProductVariant() {
    }
    public ProductVariant(Product product, String optionName, String optionValue, String sku, BigDecimal priceOverride, boolean active) {
        this.product = product;
        this.optionName = optionName;
        this.optionValue = optionValue;
        this.sku = sku;
        this.priceOverride = priceOverride;
        this.active = active;
    }
    public Long getId() {
        return id;
    }
    public Product getProduct() {
        return product;
    }
    public String getOptionName() {
        return optionName;
    }
    public String getOptionValue() {
        return optionValue;
    }
    public String getSku() {
        return sku;
    }
    public BigDecimal getPriceOverride() {
        return priceOverride;
    }
    public boolean isActive() {
        return active;
    }
    public void update(String optionName, String optionValue, String sku, BigDecimal priceOverride, boolean active) {
        this.optionName = optionName;
        this.optionValue = optionValue;
        this.sku = sku;
        this.priceOverride = priceOverride;
        this.active = active;
    }
}

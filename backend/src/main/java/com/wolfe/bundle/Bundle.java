package com.wolfe.bundle;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "product_bundles")
public class Bundle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Version private long version;
    @Column(nullable = false, unique = true, length = 120) private String slug;
    @Column(nullable = false, length = 200) private String name;
    @Column(length = 1000) private String description;
    @Column(name = "discount_type", nullable = false, length = 20) private String discountType = "PERCENT";
    @Column(name = "discount_value", nullable = false, precision = 12, scale = 2) private BigDecimal discountValue = BigDecimal.ZERO;
    @Column(nullable = false) private boolean active = true;
    protected Bundle() {
    }
    public Bundle(String slug, String name, String description, String discountType, BigDecimal discountValue, boolean active) {
        this.slug = slug;
        this.name = name;
        this.description = description;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.active = active;
    }
    public Long getId() {
        return id;
    }
    public String getSlug() {
        return slug;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public String getDiscountType() {
        return discountType;
    }
    public BigDecimal getDiscountValue() {
        return discountValue;
    }
    public boolean isActive() {
        return active;
    }
    public void configure(String slug, String name, String description, String discountType, BigDecimal discountValue, boolean active) {
        this.slug = slug.trim();
        this.name = name.trim();
        this.description = description;
        this.discountType = discountType.trim().toUpperCase();
        this.discountValue = discountValue;
        this.active = active;
    }
}

package com.wolfe.bundle;

import jakarta.persistence.*;

@Entity
@Table(name = "product_bundle_items", uniqueConstraints = @UniqueConstraint(columnNames = {
    "bundle_id", "product_id"
}
))
public class BundleItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "bundle_id", nullable = false) private Long bundleId;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(nullable = false) private int quantity = 1;
    protected BundleItem() {
    }
    public BundleItem(Long bundleId, Long productId, int quantity) {
        this.bundleId = bundleId;
        this.productId = productId;
        this.quantity = quantity;
    }
    public Long getId() {
        return id;
    }
    public Long getBundleId() {
        return bundleId;
    }
    public Long getProductId() {
        return productId;
    }
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

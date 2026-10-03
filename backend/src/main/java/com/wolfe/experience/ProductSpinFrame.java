package com.wolfe.experience;

import com.wolfe.catalog.Product;
import jakarta.persistence.*;

@Entity
@Table(name = "product_spin_frames")
public class ProductSpinFrame {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false) private Product product;
    @Column(nullable = false, length = 1200) private String imageUrl;
    @Column(nullable = false) private int sortOrder;
    protected ProductSpinFrame() {
    }
    public ProductSpinFrame(Product product, String imageUrl, int sortOrder) {
        this.product = product;
        this.imageUrl = imageUrl.trim();
        this.sortOrder = sortOrder;
    }
    public Long getId() {
        return id;
    }
    public Product getProduct() {
        return product;
    }
    public Long getProductId() { return product.getId(); }
    public String getImageUrl() {
        return imageUrl;
    }
    public int getSortOrder() {
        return sortOrder;
    }
    public void update(String imageUrl, int sortOrder) {
        this.imageUrl = imageUrl.trim();
        this.sortOrder = sortOrder;
    }
}

package com.wolfe.experience;

import com.wolfe.catalog.Product;
import jakarta.persistence.*;

@Entity
@Table(name = "product_visual_assets")
public class ProductVisualAsset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, unique = true) private Product product;
    @Column(length = 1200) private String modelUrl;
    @Column(length = 1200) private String arUrl;
    @Column(length = 1200) private String posterUrl;
    @Column(nullable = false) private boolean active = true;
    protected ProductVisualAsset() {
    }
    public ProductVisualAsset(Product product, String modelUrl, String arUrl, String posterUrl, boolean active) {
        this.product = product;
        this.modelUrl = modelUrl;
        this.arUrl = arUrl;
        this.posterUrl = posterUrl;
        this.active = active;
    }
    public Long getId() {
        return id;
    }
    public Long getProductId() {
        return product.getId();
    }
    public String getModelUrl() {
        return modelUrl;
    }
    public String getArUrl() {
        return arUrl;
    }
    public String getPosterUrl() {
        return posterUrl;
    }
    public boolean isActive() {
        return active;
    }
    public void update(String modelUrl, String arUrl, String posterUrl, boolean active) {
        this.modelUrl = modelUrl;
        this.arUrl = arUrl;
        this.posterUrl = posterUrl;
        this.active = active;
    }
}

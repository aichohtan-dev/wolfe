package com.wolfe.catalog.adminmodel;

import com.wolfe.catalog.Product;
import jakarta.persistence.*;

@Entity
@Table(name = "product_media", indexes = @Index(name = "idx_product_media_product", columnList = "product_id, sort_order"))
public class ProductMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Version private long version;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false) private Product product;
    @Column(nullable = false, length = 20) private String type;
    @Column(nullable = false, length = 2000) private String url;
    @Column(length = 500) private String altText;
    @Column(nullable = false) private int sortOrder;
    @Column(nullable = false) private boolean active = true;
    protected ProductMedia() {
    }
    public ProductMedia(Product product, String type, String url, String altText, int sortOrder, boolean active) {
        this.product = product;
        this.type = type;
        this.url = url;
        this.altText = altText;
        this.sortOrder = sortOrder;
        this.active = active;
    }
    public Long getId() {
        return id;
    }
    public Product getProduct() {
        return product;
    }
    public String getType() {
        return type;
    }
    public String getUrl() {
        return url;
    }
    public String getAltText() {
        return altText;
    }
    public int getSortOrder() {
        return sortOrder;
    }
    public boolean isActive() {
        return active;
    }
    public void deactivate() { this.active = false; }

    public void update(String type, String url, String altText, int sortOrder, boolean active) {
        this.type = type;
        this.url = url;
        this.altText = altText;
        this.sortOrder = sortOrder;
        this.active = active;
    }
}

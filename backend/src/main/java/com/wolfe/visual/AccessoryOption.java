package com.wolfe.visual;

import com.wolfe.catalog.Product;
import jakarta.persistence.*;
import java.math.*;

@Entity
@Table(name = "accessory_options")
public class AccessoryOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false) private Product product;
    @Column(nullable = false, length = 120) private String name;
    @Column(nullable = false, length = 120) private String type;
    @Column(nullable = false, length = 1200) private String overlayUrl;
    @Column(length = 120) private String sku;
    @Column(precision = 12, scale = 2) private BigDecimal price;
    @Column(nullable = false) private double x = 50, y = 50, scale = 1;
    @Column(nullable = false) private boolean active = true;
    protected AccessoryOption() {
    }
    public AccessoryOption(Product p, String name, String type, String overlayUrl, String sku, BigDecimal price, double x, double y, double scale,
    boolean active) {
        this.product = p;
        this.name = name;
        this.type = type;
        this.overlayUrl = overlayUrl;
        this.sku = sku;
        this.price = price;
        this.x = x;
        this.y = y;
        this.scale = scale;
        this.active = active;
    }
    public Long getId() {
        return id;
    }
    public Product getProduct() {
        return product;
    }
    public String getName() {
        return name;
    }
    public String getType() {
        return type;
    }
    public String getOverlayUrl() {
        return overlayUrl;
    }
    public String getSku() {
        return sku;
    }
    public BigDecimal getPrice() {
        return price;
    }
    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public double getScale() {
        return scale;
    }
    public boolean isActive() {
        return active;
    }
    public void update(String name, String type, String overlayUrl, String sku, BigDecimal price, double x, double y, double scale, boolean active) {
        this.name = name;
        this.type = type;
        this.overlayUrl = overlayUrl;
        this.sku = sku;
        this.price = price;
        this.x = x;
        this.y = y;
        this.scale = scale;
        this.active = active;
    }
}

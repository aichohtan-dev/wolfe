package com.wolfe.catalog;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true) private String slug;
    @Column(nullable = false) private String name;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal price;
    @Column(nullable = false) private String category;
    @Column(nullable = false) private String finish;
    @Column(nullable = false, length = 100) private String material = "Metal";
    @Column(nullable = false, length = 100) private String color = "Brass";
    @Column(nullable = false, length = 100) private String style = "Modern";
    @Column(length = 2000) private String description;
    @Column(name = "image_url", length = 1000) private String imageUrl;
    @Column(name = "media_urls", length = 6000) private String mediaUrls;
    @Column(nullable = false) private boolean active = true;
    @Column(nullable = false) private boolean featured = false;
    @Column(name = "sort_order", nullable = false) private int sortOrder = 0;
    protected Product() {
    }
    public Product(String slug, String name, BigDecimal price, String category, String finish, String description) {
        this.slug = slug;
        this.name = name;
        this.price = price;
        this.category = category;
        this.finish = finish;
        this.description = description;
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
    public BigDecimal getPrice() {
        return price;
    }
    public String getCategory() {
        return category;
    }
    public String getFinish() {
        return finish;
    }
    public String getDescription() {
        return description;
    }
    public String getMaterial() {
        return material;
    }
    public String getColor() {
        return color;
    }
    public String getStyle() {
        return style;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public String getMediaUrls() {
        return mediaUrls;
    }
    public void setMediaUrls(String mediaUrls) {
        this.mediaUrls = mediaUrls;
    }
    public void setMaterial(String material) {
        this.material = material;
    }
    public void setColor(String color) {
        this.color = color;
    }
    public void setStyle(String style) {
        this.style = style;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public boolean isActive() {
        return active;
    }
    public boolean isFeatured() {
        return featured;
    }
    public int getSortOrder() {
        return sortOrder;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
    public void setFeatured(boolean featured) {
        this.featured = featured;
    }
    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
    public void update(String name, BigDecimal price, String category, String finish, String description, String imageUrl, String mediaUrls, boolean active,
    boolean featured,
    int sortOrder) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.finish = finish;
        this.description = description;
        this.imageUrl = imageUrl;
        this.mediaUrls = mediaUrls;
        this.active = active;
        this.featured = featured;
        this.sortOrder = sortOrder;
    }
}

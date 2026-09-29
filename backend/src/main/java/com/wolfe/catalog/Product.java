package com.wolfe.catalog;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_products_category", columnList = "category"),
    @Index(name = "idx_products_brand", columnList = "brand_id"),
    @Index(name = "idx_products_subcategory", columnList = "subcategory"),
    @Index(name = "idx_products_cat_sub", columnList = "category, subcategory")
})
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String finish;

    @Column(nullable = false, length = 100)
    private String material = "Metal";

    @Column(nullable = false, length = 100)
    private String color = "Brass";

    @Column(nullable = false, length = 100)
    private String style = "Modern";

    @Column(length = 2000)
    private String description;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "media_urls", length = 6000)
    private String mediaUrls;

    @Column(name = "brand_id")
    private Long brandId;

    @Column(name = "brand_name", length = 120)
    private String brandName;

    @Column(length = 120)
    private String subcategory;

    @Column(length = 200)
    private String dimensions;

    @Column(name = "model_number", length = 120)
    private String modelNumber;

    @Column(name = "attributes_json", length = 12000)
    private String attributesJson;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean featured = false;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    protected Product() {}

    public Product(String slug, String name, BigDecimal price, String category, String finish, String description) {
        this.slug = slug;
        this.name = name;
        this.price = price;
        this.category = category;
        this.finish = finish;
        this.description = description;
    }

    public Long getId() { return id; }
    public String getSlug() { return slug; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public String getCategory() { return category; }
    public String getFinish() { return finish; }
    public String getDescription() { return description; }
    public String getMaterial() { return material; }
    public String getColor() { return color; }
    public String getStyle() { return style; }
    public String getImageUrl() { return imageUrl; }
    public String getMediaUrls() { return mediaUrls; }
    public Long getBrandId() { return brandId; }
    public String getBrandName() { return brandName; }
    public String getSubcategory() { return subcategory; }
    public String getDimensions() { return dimensions; }
    public String getModelNumber() { return modelNumber; }
    public String getAttributesJson() { return attributesJson; }
    public boolean isActive() { return active; }
    public boolean isFeatured() { return featured; }
    public int getSortOrder() { return sortOrder; }

    public void setSlug(String slug) { this.slug = slug; }
    public void setName(String name) { this.name = name; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setCategory(String category) { this.category = category; }
    public void setFinish(String finish) { this.finish = finish; }
    public void setDescription(String description) { this.description = description; }
    public void setMediaUrls(String mediaUrls) { this.mediaUrls = mediaUrls; }
    public void setMaterial(String material) { this.material = material; }
    public void setColor(String color) { this.color = color; }
    public void setStyle(String style) { this.style = style; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setBrandId(Long brandId) { this.brandId = brandId; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    public void setSubcategory(String subcategory) { this.subcategory = subcategory; }
    public void setDimensions(String dimensions) { this.dimensions = dimensions; }
    public void setModelNumber(String modelNumber) { this.modelNumber = modelNumber; }
    public void setAttributesJson(String attributesJson) { this.attributesJson = attributesJson; }
    public void setActive(boolean active) { this.active = active; }
    public void setFeatured(boolean featured) { this.featured = featured; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    public void update(String name, BigDecimal price, String category, String finish, String description,
                       String imageUrl, String mediaUrls, boolean active, boolean featured, int sortOrder) {
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

    public void updateFull(String name, BigDecimal price, String category, String subcategory, Long brandId, String brandName,
                           String finish, String material, String color, String style, String dimensions, String modelNumber,
                           String description, String imageUrl, String mediaUrls, String attributesJson,
                           boolean active, boolean featured, int sortOrder) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.subcategory = subcategory;
        this.brandId = brandId;
        this.brandName = brandName;
        this.finish = finish;
        this.material = material;
        this.color = color;
        this.style = style;
        this.dimensions = dimensions;
        this.modelNumber = modelNumber;
        this.description = description;
        this.imageUrl = imageUrl;
        this.mediaUrls = mediaUrls;
        this.attributesJson = attributesJson;
        this.active = active;
        this.featured = featured;
        this.sortOrder = sortOrder;
    }
}

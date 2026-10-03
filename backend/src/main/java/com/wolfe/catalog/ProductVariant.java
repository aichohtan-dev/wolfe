package com.wolfe.catalog;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "product_variants", indexes = {
    @Index(name = "idx_product_variants_product", columnList = "product_id"),
    @Index(name = "idx_variants_product_active", columnList = "product_id, active"),
    @Index(name = "idx_variants_sku", columnList = "sku")
})
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(nullable = false)
    private long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 120)
    private String optionName = "Standard";

    @Column(nullable = false, length = 180)
    private String optionValue = "Default";

    @Column(length = 200)
    private String title;

    @Column(nullable = false, length = 120, unique = true)
    private String sku;

    @Column(length = 100)
    private String color;

    @Column(length = 100)
    private String material;

    @Column(length = 100)
    private String size;

    @Column(length = 100)
    private String finish;

    @Column(length = 100)
    private String dimensions;

    @Column(name = "price_override", precision = 12, scale = 2)
    private BigDecimal priceOverride;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity = 50;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(name = "attributes_json", length = 4000)
    private String attributesJson;

    @Column(nullable = false)
    private boolean active = true;

    protected ProductVariant() {}

    public ProductVariant(Product product, String optionName, String optionValue, String sku, BigDecimal priceOverride, boolean active) {
        this.product = product;
        this.optionName = optionName != null ? optionName : "Variant";
        this.optionValue = optionValue != null ? optionValue : "Default";
        this.sku = sku;
        this.priceOverride = priceOverride;
        this.price = priceOverride;
        this.active = active;
    }

    public ProductVariant(Product product, String title, String sku, String color, String material, String size,
                          String finish, String dimensions, BigDecimal price, int stockQuantity, String imageUrl,
                          String attributesJson, boolean active, int sortOrder) {
        this.product = product;
        this.title = title;
        this.sku = sku;
        this.color = color;
        this.material = material;
        this.size = size;
        this.finish = finish;
        this.dimensions = dimensions;
        this.price = price;
        this.priceOverride = price;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
        this.attributesJson = attributesJson;
        this.active = active;
        this.sortOrder = sortOrder;
        this.optionName = (color != null && !color.isBlank()) ? "Color / Size" : "Variant";
        this.optionValue = title != null ? title : (color + " " + size);
    }

    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public String getOptionName() { return optionName; }
    public String getOptionValue() { return optionValue; }
    public String getTitle() { return title != null && !title.isBlank() ? title : optionValue; }
    public String getSku() { return sku; }
    public String getColor() { return color; }
    public String getMaterial() { return material; }
    public String getSize() { return size; }
    public String getFinish() { return finish; }
    public String getDimensions() { return dimensions; }
    public BigDecimal getPriceOverride() { return priceOverride != null ? priceOverride : price; }
    public BigDecimal getPrice() { return price != null ? price : (priceOverride != null ? priceOverride : (product != null ? product.getPrice() : BigDecimal.ZERO)); }
    public int getStockQuantity() { return stockQuantity; }
    public String getImageUrl() { return imageUrl != null && !imageUrl.isBlank() ? imageUrl : (product != null ? product.getImageUrl() : null); }
    public int getSortOrder() { return sortOrder; }
    public String getAttributesJson() { return attributesJson; }
    public boolean isActive() { return active; }

    public void setProduct(Product product) { this.product = product; }
    public void setTitle(String title) { this.title = title; }
    public void setSku(String sku) { this.sku = sku; }
    public void setColor(String color) { this.color = color; }
    public void setMaterial(String material) { this.material = material; }
    public void setSize(String size) { this.size = size; }
    public void setFinish(String finish) { this.finish = finish; }
    public void setDimensions(String dimensions) { this.dimensions = dimensions; }
    public void setPrice(BigDecimal price) { this.price = price; this.priceOverride = price; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    public void setAttributesJson(String attributesJson) { this.attributesJson = attributesJson; }
    public void setActive(boolean active) { this.active = active; }

    public void update(String optionName, String optionValue, String sku, BigDecimal priceOverride, boolean active) {
        this.optionName = optionName;
        this.optionValue = optionValue;
        this.sku = sku;
        this.priceOverride = priceOverride;
        this.price = priceOverride;
        this.active = active;
    }

    public void updateFull(String title, String sku, String color, String material, String size, String finish,
                           String dimensions, BigDecimal price, int stockQuantity, String imageUrl,
                           String attributesJson, boolean active, int sortOrder) {
        this.title = title;
        this.sku = sku;
        this.color = color;
        this.material = material;
        this.size = size;
        this.finish = finish;
        this.dimensions = dimensions;
        this.price = price;
        this.priceOverride = price;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
        this.attributesJson = attributesJson;
        this.active = active;
        this.sortOrder = sortOrder;
        this.optionValue = title != null && !title.isBlank() ? title : optionValue;
    }
}

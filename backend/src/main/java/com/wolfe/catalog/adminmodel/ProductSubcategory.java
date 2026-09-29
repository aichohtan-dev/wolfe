package com.wolfe.catalog.adminmodel;

import jakarta.persistence.*;

@Entity
@Table(name = "product_subcategories", uniqueConstraints = {
    @UniqueConstraint(name = "uq_cat_subcat", columnNames = {"category_name", "name"})
}, indexes = {
    @Index(name = "idx_subcategories_cat", columnList = "category_name, active")
})
public class ProductSubcategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "category_name", nullable = false, length = 120)
    private String categoryName;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 150)
    private String slug;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    protected ProductSubcategory() {}

    public ProductSubcategory(Long categoryId, String categoryName, String name, String slug, String description, boolean active, int sortOrder) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.active = active;
        this.sortOrder = sortOrder;
    }

    public Long getId() { return id; }
    public Long getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getDescription() { return description; }
    public boolean isActive() { return active; }
    public int getSortOrder() { return sortOrder; }

    public void update(String categoryName, String name, String slug, String description, boolean active, int sortOrder) {
        this.categoryName = categoryName;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.active = active;
        this.sortOrder = sortOrder;
    }
}

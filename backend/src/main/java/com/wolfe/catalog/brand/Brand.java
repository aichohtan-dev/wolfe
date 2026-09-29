package com.wolfe.catalog.brand;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "brands", indexes = {
    @Index(name = "idx_brands_slug", columnList = "slug"),
    @Index(name = "idx_brands_active", columnList = "active, sort_order")
})
public class Brand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String slug;

    @Column(name = "logo_url", length = 1000)
    private String logoUrl;

    @Column(length = 1000)
    private String description;

    @Column(length = 500)
    private String website;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    protected Brand() {}

    public Brand(String name, String slug, String logoUrl, String description, String website, boolean active, int sortOrder) {
        this.name = name;
        this.slug = slug;
        this.logoUrl = logoUrl;
        this.description = description;
        this.website = website;
        this.active = active;
        this.sortOrder = sortOrder;
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getLogoUrl() { return logoUrl; }
    public String getDescription() { return description; }
    public String getWebsite() { return website; }
    public boolean isActive() { return active; }
    public int getSortOrder() { return sortOrder; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public void update(String name, String slug, String logoUrl, String description, String website, boolean active, int sortOrder) {
        this.name = name;
        this.slug = slug;
        this.logoUrl = logoUrl;
        this.description = description;
        this.website = website;
        this.active = active;
        this.sortOrder = sortOrder;
    }
}

package com.wolfe.catalog.adminmodel;

import jakarta.persistence.*;

@Entity
@Table(name = "product_categories")
public class ProductCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 120) private String name;
    @Column(nullable = false) private boolean active = true;
    protected ProductCategory() {
    }
    public ProductCategory(String name) {
        this.name = name;
    }
    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public boolean isActive() {
        return active;
    }
    public void update(String name, boolean active) {
        this.name = name;
        this.active = active;
    }
}

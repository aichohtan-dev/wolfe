package com.wolfe.catalog.adminmodel;

import com.wolfe.catalog.Product;
import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "product_collections")
public class ProductCollection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 120) private String name;
    @Column(length = 500) private String description;
    @Column(nullable = false) private boolean active = true;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "product_collection_items", joinColumns = @JoinColumn(name = "collection_id"),
    inverseJoinColumns = @JoinColumn(name = "product_id")) private Set<Product> products = new LinkedHashSet<>();
    protected ProductCollection() {
    }
    public ProductCollection(String name, String description) {
        this.name = name;
        this.description = description;
    }
    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public boolean isActive() {
        return active;
    }
    public Set<Product> getProducts() {
        return products;
    }
    public void update(String name, String description, boolean active) {
        this.name = name;
        this.description = description;
        this.active = active;
    }
}

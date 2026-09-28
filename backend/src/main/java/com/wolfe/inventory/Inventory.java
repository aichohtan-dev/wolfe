package com.wolfe.inventory;

import com.wolfe.catalog.Product;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "inventory")
public class Inventory {
    @Id
    @Column(name = "product_id")
    private Long productId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false) private int quantity;
    @Column(nullable = false) private int reserved;
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;
    protected Inventory() {
    }
    public Inventory(Product product, int quantity) {
        this.product = product;
        this.productId = product != null ? product.getId() : null;
        this.quantity = quantity;
        this.reserved = 0;
        this.updatedAt = OffsetDateTime.now();
    }
    public Long getProductId() {
        return productId != null ? productId : (product != null ? product.getId() : null);
    }
    public Product getProduct() {
        return product;
    }
    public int getQuantity() {
        return quantity;
    }
    public int getReserved() {
        return reserved;
    }
    public int getAvailable() {
        return quantity-reserved;
    }
    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setQuantity(int quantity) {
        if (quantity<reserved)throw new IllegalArgumentException("quantity cannot be below reserved");
        this.quantity = quantity;
        touch();
    }
    public void setReserved(int reserved) {
        if (reserved<0 || reserved>quantity)throw new IllegalArgumentException("invalid reserved quantity");
        this.reserved = reserved;
        touch();
    }
    public void reserve(int amount) {
        if (amount<1)throw new IllegalArgumentException("reservation quantity must be positive");
        if (getAvailable()<amount)throw new IllegalArgumentException("insufficient stock for "+product.getSlug());
        reserved += amount;
        touch();
    }
    public void release(int amount) {
        if (amount<0 || amount>reserved)throw new IllegalArgumentException("invalid release quantity");
        reserved-=amount;
        touch();
    }
    public void fulfill(int amount) {
        if (amount<1 || amount>reserved || amount>quantity)throw new IllegalArgumentException("invalid fulfillment quantity");
        reserved-=amount;
        quantity-=amount;
        touch();
    }
    private void touch() {
        updatedAt = OffsetDateTime.now();
    }
}

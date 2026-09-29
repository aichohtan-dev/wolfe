package com.wolfe.retailer;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "retailer_inventory", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"retailer_id", "sku"})
})
public class RetailerInventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "retailer_id", nullable = false)
    private Long retailerId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "variant_id")
    private Long variantId;

    @Column(nullable = false, length = 100)
    private String sku;

    @Column(name = "physical_stock", nullable = false)
    private int physicalStock = 0;

    @Column(name = "reserved_stock", nullable = false)
    private int reservedStock = 0;

    @Column(name = "available_stock", nullable = false)
    private int availableStock = 0;

    @Column(name = "low_stock_threshold", nullable = false)
    private int lowStockThreshold = 5;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public RetailerInventory() {}

    public RetailerInventory(Long retailerId, Long productId, Long variantId, String sku, int physicalStock, int lowStockThreshold) {
        this.retailerId = retailerId;
        this.productId = productId;
        this.variantId = variantId;
        this.sku = sku;
        this.physicalStock = Math.max(0, physicalStock);
        this.reservedStock = 0;
        this.availableStock = this.physicalStock;
        this.lowStockThreshold = lowStockThreshold > 0 ? lowStockThreshold : 5;
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public Long getRetailerId() { return retailerId; }
    public Long getProductId() { return productId; }
    public Long getVariantId() { return variantId; }
    public String getSku() { return sku; }
    public int getPhysicalStock() { return physicalStock; }
    public int getReservedStock() { return reservedStock; }
    public int getAvailableStock() { return availableStock; }
    public int getLowStockThreshold() { return lowStockThreshold; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void setLowStockThreshold(int lowStockThreshold) {
        this.lowStockThreshold = Math.max(0, lowStockThreshold);
        touch();
    }

    public void reserve(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Reservation quantity must be positive");
        if (this.availableStock < quantity) {
            throw new IllegalArgumentException("Insufficient available stock for SKU " + sku + " (Available: " + availableStock + ", Requested: " + quantity + ")");
        }
        this.reservedStock += quantity;
        this.availableStock = this.physicalStock - this.reservedStock;
        touch();
    }

    public void release(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Release quantity must be positive");
        if (this.reservedStock < quantity) {
            throw new IllegalArgumentException("Cannot release more than reserved stock (Reserved: " + reservedStock + ", Requested: " + quantity + ")");
        }
        this.reservedStock -= quantity;
        this.availableStock = this.physicalStock - this.reservedStock;
        touch();
    }

    public void fulfill(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Fulfillment quantity must be positive");
        if (this.reservedStock < quantity || this.physicalStock < quantity) {
            throw new IllegalArgumentException("Cannot fulfill quantity exceeding physical or reserved stock");
        }
        this.reservedStock -= quantity;
        this.physicalStock -= quantity;
        this.availableStock = this.physicalStock - this.reservedStock;
        touch();
    }

    public void adjustPhysicalStock(int newPhysicalStock) {
        if (newPhysicalStock < this.reservedStock) {
            throw new IllegalArgumentException("Physical stock (" + newPhysicalStock + ") cannot be set below currently reserved stock (" + this.reservedStock + ")");
        }
        this.physicalStock = newPhysicalStock;
        this.availableStock = this.physicalStock - this.reservedStock;
        touch();
    }

    public boolean isLowStock() {
        return this.availableStock <= this.lowStockThreshold;
    }

    private void touch() {
        this.updatedAt = OffsetDateTime.now();
    }
}

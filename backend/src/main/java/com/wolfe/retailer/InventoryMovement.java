package com.wolfe.retailer;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "inventory_movements")
public class InventoryMovement {
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

    @Column(name = "previous_quantity", nullable = false)
    private int previousQuantity;

    @Column(name = "quantity_changed", nullable = false)
    private int quantityChanged;

    @Column(name = "new_quantity", nullable = false)
    private int newQuantity;

    @Column(name = "movement_type", nullable = false, length = 50)
    private String movementType; // INITIAL, ADJUSTMENT, RESERVATION, RESERVATION_RELEASE, FULFILLMENT, RETURN, CORRECTION

    @Column(name = "order_id", length = 50)
    private String orderId;

    @Column(name = "created_by", length = 150)
    private String createdBy;

    @Column(length = 500)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public InventoryMovement() {}

    public InventoryMovement(Long retailerId, Long productId, Long variantId, String sku, int previousQuantity, int quantityChanged, int newQuantity, String movementType, String orderId, String createdBy, String reason) {
        this.retailerId = retailerId;
        this.productId = productId;
        this.variantId = variantId;
        this.sku = sku;
        this.previousQuantity = previousQuantity;
        this.quantityChanged = quantityChanged;
        this.newQuantity = newQuantity;
        this.movementType = movementType;
        this.orderId = orderId;
        this.createdBy = createdBy != null ? createdBy : "SYSTEM";
        this.reason = reason;
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public Long getRetailerId() { return retailerId; }
    public Long getProductId() { return productId; }
    public Long getVariantId() { return variantId; }
    public String getSku() { return sku; }
    public int getPreviousQuantity() { return previousQuantity; }
    public int getQuantityChanged() { return quantityChanged; }
    public int getNewQuantity() { return newQuantity; }
    public String getMovementType() { return movementType; }
    public String getOrderId() { return orderId; }
    public String getCreatedBy() { return createdBy; }
    public String getReason() { return reason; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}

package com.wolfe.retailer;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "fulfillments")
public class Fulfillment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, length = 50)
    private String orderId;

    @Column(name = "retailer_id", nullable = false)
    private Long retailerId;

    @Column(nullable = false, length = 50)
    private String status = "ASSIGNED"; // ASSIGNED, ACCEPTED, PACKED, READY_FOR_DELIVERY, OUT_FOR_DELIVERY, DELIVERED, CANCELLED, FAILED_DELIVERY, REASSIGNED, RETURNED

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "courier_name", length = 100)
    private String courierName = "Wolfe Local Logistics";

    @Column(name = "packed_at")
    private OffsetDateTime packedAt;

    @Column(name = "shipped_at")
    private OffsetDateTime shippedAt;

    @Column(name = "delivered_at")
    private OffsetDateTime deliveredAt;

    @Column(name = "failed_at")
    private OffsetDateTime failedAt;

    @Column(name = "failed_reason", length = 500)
    private String failedReason;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public Fulfillment() {}

    public Fulfillment(String orderId, Long retailerId) {
        this.orderId = orderId;
        this.retailerId = retailerId;
        this.status = "ASSIGNED";
        this.courierName = "Wolfe Local Logistics";
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public String getOrderId() { return orderId; }
    public Long getRetailerId() { return retailerId; }
    public void setRetailerId(Long retailerId) { this.retailerId = retailerId; touch(); }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; touch(); }
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; touch(); }
    public String getCourierName() { return courierName; }
    public void setCourierName(String courierName) { this.courierName = courierName; touch(); }
    public OffsetDateTime getPackedAt() { return packedAt; }
    public void setPackedAt(OffsetDateTime packedAt) { this.packedAt = packedAt; touch(); }
    public OffsetDateTime getShippedAt() { return shippedAt; }
    public void setShippedAt(OffsetDateTime shippedAt) { this.shippedAt = shippedAt; touch(); }
    public OffsetDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(OffsetDateTime deliveredAt) { this.deliveredAt = deliveredAt; touch(); }
    public OffsetDateTime getFailedAt() { return failedAt; }
    public void setFailedAt(OffsetDateTime failedAt) { this.failedAt = failedAt; touch(); }
    public String getFailedReason() { return failedReason; }
    public void setFailedReason(String failedReason) { this.failedReason = failedReason; touch(); }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    private void touch() {
        this.updatedAt = OffsetDateTime.now();
    }
}

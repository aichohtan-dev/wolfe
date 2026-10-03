package com.wolfe.returning;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "return_requests")
public class ReturnRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Version private long version;
    @Column(name = "order_id", nullable = false) private String orderId;
    @Column(name = "customer_id", nullable = false) private Long customerId;
    @Column(nullable = false, length = 1000) private String reason;
    @Column(name = "item_quantities_json", columnDefinition = "TEXT") private String itemQuantitiesJson;
    @Column(nullable = false) private String status = "PENDING";
    @Column(name = "refund_amount", nullable = false) private long refundAmount;
    @Column(name = "refund_status", nullable = false) private String refundStatus = "NOT_REQUESTED";
    @Column(name = "admin_note", length = 1000) private String adminNote;
    @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();
    @Column(name = "updated_at", nullable = false) private Instant updatedAt = Instant.now();
    protected ReturnRequest() {
    }
    public ReturnRequest(String orderId, Long customerId, String reason) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.reason = reason.trim();
    }
    public Long getId() {
        return id;
    }
    public String getOrderId() {
        return orderId;
    }
    public Long getCustomerId() {
        return customerId;
    }
    public String getReason() { return reason; }
    public String getItemQuantitiesJson() { return itemQuantitiesJson; }
    public void setItemQuantitiesJson(String json) { this.itemQuantitiesJson = json; this.updatedAt = Instant.now(); }
    public String getStatus() {
        return status;
    }
    public long getRefundAmount() {
        return refundAmount;
    }
    public String getRefundStatus() {
        return refundStatus;
    }
    public String getAdminNote() {
        return adminNote;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    public String getRestockStatus() { return restockStatus; }
    public String getSettlementAdjustmentStatus() { return settlementAdjustmentStatus; }
    public Instant getCompletedAt() { return completedAt; }
    @Column(name = "restock_status", nullable = false) private String restockStatus = "NOT_PROCESSED";
    @Column(name = "settlement_adjustment_status", nullable = false) private String settlementAdjustmentStatus = "NOT_PROCESSED";
    @Column(name = "completed_at") private Instant completedAt;

    public void update(String status, long refundAmount, String refundStatus, String adminNote) {
        String next = status == null ? "" : status.toUpperCase();
        String current = this.status == null ? "PENDING" : this.status.toUpperCase();
        java.util.Map<String, java.util.Set<String>> allowed = java.util.Map.of(
                "PENDING", java.util.Set.of("APPROVED", "REJECTED"),
                "APPROVED", java.util.Set.of("RECEIVED", "REJECTED"),
                "RECEIVED", java.util.Set.of("COMPLETED"),
                "REJECTED", java.util.Set.of(),
                "COMPLETED", java.util.Set.of());
        if (!current.equals(next) && !allowed.getOrDefault(current, java.util.Set.of()).contains(next)) {
            throw new IllegalStateException("Invalid return transition: " + current + " -> " + next);
        }
        if ("COMPLETED".equals(next) && !"REFUNDED".equals(refundStatus)) throw new IllegalArgumentException("completed return requires REFUNDED refund status");
        if ("REFUNDED".equals(refundStatus) && refundAmount <= 0) throw new IllegalArgumentException("refunded return must have a positive refund amount");
        if ("REFUNDED".equalsIgnoreCase(this.refundStatus) && (refundAmount != this.refundAmount || !"REFUNDED".equalsIgnoreCase(refundStatus))) {
            throw new IllegalStateException("A completed refund cannot be altered");
        }
        this.status = next; this.refundAmount = refundAmount; this.refundStatus = refundStatus; this.adminNote = adminNote;
        if ("COMPLETED".equals(next)) this.completedAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    public void markRestocked() { this.restockStatus = "RESTOCKED"; this.updatedAt = Instant.now(); }
    public void markSettlementAdjusted() { this.settlementAdjustmentStatus = "ADJUSTED"; this.updatedAt = Instant.now(); }
}

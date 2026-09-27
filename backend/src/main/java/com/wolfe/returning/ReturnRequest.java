package com.wolfe.returning;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "return_requests")
public class ReturnRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "order_id", nullable = false, unique = true) private String orderId;
    @Column(name = "customer_id", nullable = false) private Long customerId;
    @Column(nullable = false, length = 1000) private String reason;
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
    public String getReason() {
        return reason;
    }
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
    public void update(String status, long refundAmount, String refundStatus, String adminNote) {
        this.status = status;
        this.refundAmount = refundAmount;
        this.refundStatus = refundStatus;
        this.adminNote = adminNote;
        this.updatedAt = Instant.now();
    }
}

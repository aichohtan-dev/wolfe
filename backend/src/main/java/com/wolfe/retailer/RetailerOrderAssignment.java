package com.wolfe.retailer;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "retailer_order_assignments")
public class RetailerOrderAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, length = 50)
    private String orderId;

    @Column(name = "retailer_id", nullable = false)
    private Long retailerId;

    @Column(nullable = false, length = 50)
    private String status = "ASSIGNED"; // ASSIGNED, ACCEPTED, REJECTED, REASSIGNED, COMPLETED, CANCELLED

    @Column(name = "assigned_by", nullable = false, length = 100)
    private String assignedBy = "AUTO_ALLOCATION";

    @Column(name = "assigned_at", nullable = false)
    private OffsetDateTime assignedAt = OffsetDateTime.now();

    @Column(name = "accepted_at")
    private OffsetDateTime acceptedAt;

    @Column(name = "rejected_at")
    private OffsetDateTime rejectedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(length = 1000)
    private String notes;

    public RetailerOrderAssignment() {}

    public RetailerOrderAssignment(String orderId, Long retailerId, String assignedBy, String notes) {
        this.orderId = orderId;
        this.retailerId = retailerId;
        this.assignedBy = assignedBy != null ? assignedBy : "AUTO_ALLOCATION";
        this.status = "ASSIGNED";
        this.notes = notes;
        this.assignedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public String getOrderId() { return orderId; }
    public Long getRetailerId() { return retailerId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAssignedBy() { return assignedBy; }
    public OffsetDateTime getAssignedAt() { return assignedAt; }
    public OffsetDateTime getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(OffsetDateTime acceptedAt) { this.acceptedAt = acceptedAt; }
    public OffsetDateTime getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(OffsetDateTime rejectedAt) { this.rejectedAt = rejectedAt; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public void accept() {
        this.status = "ACCEPTED";
        this.acceptedAt = OffsetDateTime.now();
    }

    public void reject(String reason) {
        this.status = "REJECTED";
        this.rejectionReason = reason;
        this.rejectedAt = OffsetDateTime.now();
    }
}

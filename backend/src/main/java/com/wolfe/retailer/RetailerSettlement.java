package com.wolfe.retailer;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "retailer_settlements")
public class RetailerSettlement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, length = 50)
    private String orderId;

    @Column(name = "retailer_id", nullable = false)
    private Long retailerId;

    @Column(name = "gross_amount", nullable = false)
    private long grossAmount; // in paise

    @Column(name = "wolfe_margin_amount", nullable = false)
    private long wolfeMarginAmount; // in paise

    @Column(name = "retailer_payable_amount", nullable = false)
    private long retailerPayableAmount; // in paise

    @Column(nullable = false, length = 50)
    private String status = "PENDING"; // PENDING, ELIGIBLE, PROCESSING, SETTLED, HELD, ADJUSTED

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "settled_at")
    private OffsetDateTime settledAt;

    @Column(length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public RetailerSettlement() {}

    public RetailerSettlement(String orderId, Long retailerId, long grossAmount, long wolfeMarginAmount, long retailerPayableAmount) {
        this.orderId = orderId;
        this.retailerId = retailerId;
        this.grossAmount = grossAmount;
        this.wolfeMarginAmount = wolfeMarginAmount;
        this.retailerPayableAmount = retailerPayableAmount;
        this.status = "PENDING";
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public String getOrderId() { return orderId; }
    public Long getRetailerId() { return retailerId; }
    public long getGrossAmount() { return grossAmount; }
    public long getWolfeMarginAmount() { return wolfeMarginAmount; }
    public long getRetailerPayableAmount() { return retailerPayableAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; touch(); }
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; touch(); }
    public OffsetDateTime getSettledAt() { return settledAt; }
    public void setSettledAt(OffsetDateTime settledAt) { this.settledAt = settledAt; touch(); }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; touch(); }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void markEligible() {
        if ("PENDING".equals(this.status)) {
            this.status = "ELIGIBLE";
            touch();
        }
    }

    public void markSettled(String referenceNumber) {
        this.status = "SETTLED";
        this.referenceNumber = referenceNumber;
        this.settledAt = OffsetDateTime.now();
        touch();
    }

    private void touch() {
        this.updatedAt = OffsetDateTime.now();
    }
}

package com.wolfe.retailer;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.Set;

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

    @Column(name = "settled_amount", nullable = false)
    private long settledAmount;

    @Column(length = 500)
    private String notes;

    @Column(name = "adjustment_amount", nullable = false)
    private long adjustmentAmount;
    @Column(name = "recovery_due_amount", nullable = false)
    private long recoveryDueAmount;
    @Column(name = "cash_expected_amount", nullable = false) private long cashExpectedAmount;
    @Column(name = "cash_collected_amount", nullable = false) private long cashCollectedAmount;
    @Column(name = "cash_deposited_amount", nullable = false) private long cashDepositedAmount;
    @Column(name = "cash_reconciliation_status", nullable = false, length = 30) private String cashReconciliationStatus = "NOT_REQUIRED";
    @Column(name = "cash_reconciliation_reference", length = 100) private String cashReconciliationReference;

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
    public void setId(Long id) { this.id = id; }
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
    public long getSettledAmount() { return settledAmount; }
    public void setSettledAt(OffsetDateTime settledAt) { this.settledAt = settledAt; touch(); }
    public String getNotes() { return notes; }
    public long getAdjustmentAmount() { return adjustmentAmount; }
    public long getRecoveryDueAmount() { return recoveryDueAmount; }
    public long getCashExpectedAmount() { return cashExpectedAmount; }
    public long getCashCollectedAmount() { return cashCollectedAmount; }
    public long getCashDepositedAmount() { return cashDepositedAmount; }
    public String getCashReconciliationStatus() { return cashReconciliationStatus; }
    public String getCashReconciliationReference() { return cashReconciliationReference; }
    public void setNotes(String notes) { this.notes = notes; touch(); }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void setCashExpected(long expected) {
        if (expected < 0) throw new IllegalArgumentException("cash expected cannot be negative");
        this.cashExpectedAmount = expected;
        this.cashReconciliationStatus = "PENDING";
        touch();
    }

    public void reconcileCash(long expected, long collected, long deposited, String reference) {
        if (expected != this.cashExpectedAmount) throw new IllegalArgumentException("cash expected amount does not match the server value");
        if (expected < 0 || collected < 0 || deposited < 0) throw new IllegalArgumentException("cash amounts cannot be negative");
        if (collected != expected || deposited != expected) throw new IllegalArgumentException("COD cash collected and deposited must equal expected amount");
        if (reference == null || reference.isBlank()) throw new IllegalArgumentException("cash reconciliation reference is required");
        this.cashExpectedAmount = expected; this.cashCollectedAmount = collected; this.cashDepositedAmount = deposited;
        this.cashReconciliationReference = reference.trim(); this.cashReconciliationStatus = "RECONCILED"; touch();
    }

    public void markEligible() {
        if ("PENDING".equals(this.status)) {
            this.status = "ELIGIBLE";
            touch();
        }
    }

    public void markSettled(String referenceNumber) {
        if (!"ELIGIBLE".equals(this.status)) {
            throw new IllegalStateException("Settlement must be ELIGIBLE before it can be settled");
        }
        long netPayable = Math.max(0L, retailerPayableAmount - adjustmentAmount);
        if (recoveryDueAmount > 0) throw new IllegalStateException("Recovery must be collected before settlement");
        this.status = "SETTLED";
        this.settledAmount = netPayable;
        this.referenceNumber = referenceNumber;
        this.settledAt = OffsetDateTime.now();
        touch();
    }

    public record ReturnAdjustment(long appliedAmount, long recoveryAmount) {}

    public ReturnAdjustment markAdjustedForReturn(long amount, String note) {
        long safeAmount = Math.max(0, amount);
        // Before payout (PENDING/ELIGIBLE/ADJUSTED), a return can be netted from the
        // remaining retailer payable. Once the settlement is SETTLED or already has
        // recovery due, no payable remains to net; the full new adjustment becomes a
        // recovery obligation. This also supports multiple partial returns for one order.
        long available = Set.of("SETTLED", "RECOVERY_DUE").contains(this.status)
                ? 0L
                : Math.max(0, retailerPayableAmount - adjustmentAmount);
        long applied = Math.min(available, safeAmount);
        this.adjustmentAmount = Math.addExact(this.adjustmentAmount, applied);
        long newRecovery = Math.max(0, safeAmount - applied);
        this.recoveryDueAmount = Math.addExact(this.recoveryDueAmount, newRecovery);
        if (recoveryDueAmount > 0) {
            this.status = "RECOVERY_DUE";
            this.notes = note + "; recovery due=" + recoveryDueAmount + " paise";
        } else if ("PENDING".equalsIgnoreCase(this.status) || "ELIGIBLE".equalsIgnoreCase(this.status)) {
            // A return before payout changes the payable amount but must not strand the
            // settlement in ADJUSTED: the normal ELIGIBLE -> SETTLED flow must remain usable.
            this.status = "ELIGIBLE";
            this.notes = note;
        } else {
            this.status = "ADJUSTED";
            this.notes = note;
        }
        touch();
        return new ReturnAdjustment(applied, newRecovery);
    }

    public void markRecoveryCollected(String reference) {
        if (recoveryDueAmount <= 0) throw new IllegalStateException("No recovery amount is due");
        if (reference == null || reference.isBlank()) throw new IllegalArgumentException("recovery reference is required");
        recoveryDueAmount = 0;
        // Recovery is a cash collection of a post-payout return adjustment. Once the
        // recovery is collected there is no remaining recovery blocker, so the settlement
        // must return to ELIGIBLE for the normal payout flow rather than getting stranded
        // in ADJUSTED (markSettled only accepts ELIGIBLE).
        status = "ELIGIBLE";
        notes = (notes == null ? "" : notes + "; ") + "Recovery collected: " + reference.trim();
        touch();
    }

    public void markAdjustedForReassignment() {
        if ("SETTLED".equals(this.status)) {
            throw new IllegalStateException("Cannot reassign an already settled retailer payout");
        }
        this.status = "ADJUSTED";
        touch();
    }

    private void touch() {
        this.updatedAt = OffsetDateTime.now();
    }
}

package com.wolfe.retailer;

import com.wolfe.catalog.Product;
import com.wolfe.catalog.ProductVariant;
import com.wolfe.order.OrderItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
public class RetailerSettlementService {
    private final RetailerMarginRuleRepository marginRepo;
    private final RetailerSettlementRepository settlementRepo;
    private final RetailerRepository retailerRepo;
    private final RetailerAuditLogRepository auditRepo;
    private final RetailerSettlementAdjustmentRepository adjustmentRepo;

    public RetailerSettlementService(RetailerMarginRuleRepository marginRepo,
                                   RetailerSettlementRepository settlementRepo,
                                   RetailerRepository retailerRepo,
                                   RetailerAuditLogRepository auditRepo,
                                   RetailerSettlementAdjustmentRepository adjustmentRepo) {
        this.marginRepo = marginRepo;
        this.settlementRepo = settlementRepo;
        this.retailerRepo = retailerRepo;
        this.auditRepo = auditRepo;
        this.adjustmentRepo = adjustmentRepo;
    }

    public record MarginCalculation(long grossAmount, long wolfeMarginAmount, long retailerPayableAmount, String ruleApplied) {}

    public MarginCalculation calculateLineItemSettlement(Long retailerId, String category, Long productId, Long variantId, long lineItemGross) {
        if (lineItemGross <= 0) throw new IllegalArgumentException("line item gross must be positive");
        List<RetailerMarginRule> matchingRules = marginRepo.findMatchingRules(retailerId, variantId, productId, category);

        BigDecimal marginPercent = new BigDecimal("10.0");
        String ruleDesc = "Global Default (10%)";

        if (!matchingRules.isEmpty()) {
            RetailerMarginRule bestRule = matchingRules.get(0);
            if ("PERCENTAGE".equalsIgnoreCase(bestRule.getMarginType())) {
                marginPercent = bestRule.getMarginValue();
                ruleDesc = "Rule #" + bestRule.getId() + " (" + marginPercent + "%)";
            } else if ("FIXED".equalsIgnoreCase(bestRule.getMarginType())) {
                long fixedPaise = bestRule.getMarginValue().movePointRight(2).longValueExact();
                long wolfeMargin = Math.min(lineItemGross, fixedPaise);
                long retailerPayout = lineItemGross - wolfeMargin;
                return new MarginCalculation(lineItemGross, wolfeMargin, retailerPayout, "Fixed Margin Rule #" + bestRule.getId());
            }
        } else if (retailerId != null) {
            Optional<Retailer> ret = retailerRepo.findById(retailerId);
            if (ret.isPresent() && ret.get().getCommissionRate() != null) {
                marginPercent = ret.get().getCommissionRate();
                ruleDesc = "Retailer Default (" + marginPercent + "%)";
            }
        }

        long wolfeMargin = BigDecimal.valueOf(lineItemGross)
                .multiply(marginPercent)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                .longValueExact();

        wolfeMargin = Math.min(lineItemGross, Math.max(0, wolfeMargin));
        long retailerPayout = lineItemGross - wolfeMargin;

        return new MarginCalculation(lineItemGross, wolfeMargin, retailerPayout, ruleDesc);
    }

    @Transactional
    public RetailerSettlement initializeSettlement(String orderId, Long retailerId, long grossAmount, long wolfeMarginAmount, long retailerPayableAmount) {
        Optional<RetailerSettlement> existing = settlementRepo.findTopByOrderIdAndRetailerIdOrderByIdDesc(orderId, retailerId);
        if (existing.isPresent() && !"ADJUSTED".equalsIgnoreCase(existing.get().getStatus())) {
            return existing.get();
        }

        RetailerSettlement settlement = new RetailerSettlement(orderId, retailerId, grossAmount, wolfeMarginAmount, retailerPayableAmount);
        RetailerSettlement saved = settlementRepo.save(settlement);

        auditRepo.save(new RetailerAuditLog(
                "RetailerSettlement", String.valueOf(saved.getId()),
                "CREATE_SETTLEMENT", "SYSTEM", "Initialized settlement for order " + orderId + ": Gross=₹" + (grossAmount/100) + ", Wolfe Margin=₹" + (wolfeMarginAmount/100) + ", Retailer Payable=₹" + (retailerPayableAmount/100)
        ));

        return saved;
    }

    @Transactional
    public RetailerSettlement markEligible(String orderId, Long retailerId) {
        RetailerSettlement s = settlementRepo.findByOrderIdAndRetailerIdForUpdateRows(orderId, retailerId).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found for order and retailer"));
        if (!"PENDING".equalsIgnoreCase(s.getStatus())) {
            throw new IllegalStateException("Settlement cannot become eligible from status: " + s.getStatus());
        }
        s.markEligible();
        return settlementRepo.save(s);
    }

    @Transactional
    public RetailerSettlement markSettled(Long settlementId, String referenceNumber, String actor) {
        RetailerSettlement s = settlementRepo.findByIdForUpdate(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found with ID: " + settlementId));
        if (!"ELIGIBLE".equalsIgnoreCase(s.getStatus())) {
            throw new IllegalStateException("Settlement is not eligible for payment");
        }
        if (referenceNumber == null || referenceNumber.isBlank()) {
            throw new IllegalArgumentException("settlement reference number is required");
        }
        if (s.getCashExpectedAmount() > 0 && !"RECONCILED".equalsIgnoreCase(s.getCashReconciliationStatus())) {
            throw new IllegalStateException("COD cash must be reconciled before settlement");
        }
        s.markSettled(referenceNumber.trim());
        RetailerSettlement saved = settlementRepo.save(s);

        auditRepo.save(new RetailerAuditLog(
                "RetailerSettlement", String.valueOf(saved.getId()),
                "SETTLE_PAYMENT", actor, "Marked settlement as SETTLED with reference: " + referenceNumber
        ));

        return saved;
    }

    @Transactional
    public RetailerSettlement reconcileCash(Long settlementId, long expected, long collected, long deposited, String reference, String actor) {
        RetailerSettlement s = settlementRepo.findByIdForUpdate(settlementId).orElseThrow(() -> new IllegalArgumentException("Settlement not found"));
        if (!"ELIGIBLE".equalsIgnoreCase(s.getStatus())) throw new IllegalStateException("COD cash can only be reconciled for an ELIGIBLE settlement");
        if (expected != s.getCashExpectedAmount()) throw new IllegalArgumentException("cash expected amount must match the server-calculated order amount");
        s.reconcileCash(expected, collected, deposited, reference);
        RetailerSettlement saved = settlementRepo.save(s);
        auditRepo.save(new RetailerAuditLog("RetailerSettlement", String.valueOf(saved.getId()), "RECONCILE_COD_CASH", actor, "COD cash reconciled"));
        return saved;
    }

    @Transactional
    public RetailerSettlement setCashExpected(Long settlementId, long expected) {
        RetailerSettlement s = settlementRepo.findByIdForUpdate(settlementId).orElseThrow(() -> new IllegalArgumentException("Settlement not found"));
        s.setCashExpected(expected);
        return settlementRepo.save(s);
    }

    @Transactional
    public void markAdjustedForReturn(String orderId, Long retailerId, long refundAmount, String actor) {
        RetailerSettlement s = settlementRepo.findByOrderIdAndRetailerIdForUpdateRows(orderId, retailerId).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Retailer settlement is missing for completed retailer return: order=" + orderId + ", retailer=" + retailerId));
            long retailerAdjustment = s.getGrossAmount() <= 0 ? 0 :
                    java.math.BigDecimal.valueOf(Math.max(0, refundAmount))
                            .multiply(java.math.BigDecimal.valueOf(s.getRetailerPayableAmount()))
                            .divide(java.math.BigDecimal.valueOf(s.getGrossAmount()), 0, java.math.RoundingMode.HALF_UP)
                            .longValueExact();
            retailerAdjustment = Math.min(retailerAdjustment, s.getRetailerPayableAmount());
            // Each completed ReturnRequest is its own financial event. The ReturnProcessingService
            // is idempotent per return, so do not collapse multiple legitimate partial returns into
            // one adjustment record for the settlement.
            RetailerSettlement.ReturnAdjustment result = s.markAdjustedForReturn(retailerAdjustment, "Return completed for order " + orderId);
            if (result.appliedAmount() > 0) {
                adjustmentRepo.save(new RetailerSettlementAdjustment(s.getId(), orderId, retailerId, result.appliedAmount(), "RETURN",
                        "Return completed for order " + orderId, actor));
            }
            settlementRepo.save(s);
        auditRepo.save(new RetailerAuditLog("RetailerSettlement", String.valueOf(s.getId()), "ADJUST_FOR_RETURN", actor,
                "Settlement adjusted for return/refund of " + refundAmount + " paise"));
    }

    @Transactional
    public RetailerSettlement collectRecovery(Long settlementId, String reference, String actor) {
        RetailerSettlement s = settlementRepo.findByIdForUpdate(settlementId).orElseThrow(() -> new IllegalArgumentException("Settlement not found"));
        s.markRecoveryCollected(reference);
        RetailerSettlement saved = settlementRepo.save(s);
        auditRepo.save(new RetailerAuditLog("RetailerSettlement", String.valueOf(saved.getId()), "COLLECT_RETURN_RECOVERY", actor, "Collected retailer return recovery"));
        return saved;
    }

    @Transactional
    public void markAdjustedForReassignment(String orderId, Long retailerId, String actor) {
        RetailerSettlement s = settlementRepo.findByOrderIdAndRetailerIdForUpdateRows(orderId, retailerId).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Retailer settlement is missing for retailer reassignment: order=" + orderId + ", retailer=" + retailerId));
        if ("SETTLED".equalsIgnoreCase(s.getStatus()) || "ADJUSTED".equalsIgnoreCase(s.getStatus()) || "RECOVERY_DUE".equalsIgnoreCase(s.getStatus())) {
                throw new IllegalStateException("Cannot reassign a settled or already adjusted retailer settlement");
            }
            s.markAdjustedForReassignment();
            settlementRepo.save(s);
        auditRepo.save(new RetailerAuditLog(
                "RetailerSettlement", String.valueOf(s.getId()),
                "ADJUST_FOR_REASSIGNMENT", actor,
                "Settlement superseded by retailer reassignment for order " + orderId
        ));
    }
}

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

    public RetailerSettlementService(RetailerMarginRuleRepository marginRepo,
                                   RetailerSettlementRepository settlementRepo,
                                   RetailerRepository retailerRepo,
                                   RetailerAuditLogRepository auditRepo) {
        this.marginRepo = marginRepo;
        this.settlementRepo = settlementRepo;
        this.retailerRepo = retailerRepo;
        this.auditRepo = auditRepo;
    }

    public record MarginCalculation(long grossAmount, long wolfeMarginAmount, long retailerPayableAmount, String ruleApplied) {}

    public MarginCalculation calculateLineItemSettlement(Long retailerId, String category, Long productId, Long variantId, long lineItemGross) {
        List<RetailerMarginRule> matchingRules = marginRepo.findMatchingRules(retailerId, variantId, productId, category);

        BigDecimal marginPercent = new BigDecimal("10.0");
        String ruleDesc = "Global Default (10%)";

        if (!matchingRules.isEmpty()) {
            RetailerMarginRule bestRule = matchingRules.get(0);
            if ("PERCENTAGE".equalsIgnoreCase(bestRule.getMarginType())) {
                marginPercent = bestRule.getMarginValue();
                ruleDesc = "Rule #" + bestRule.getId() + " (" + marginPercent + "%)";
            } else if ("FIXED".equalsIgnoreCase(bestRule.getMarginType())) {
                long fixedPaise = bestRule.getMarginValue().movePointRight(2).longValue();
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
        Optional<RetailerSettlement> existing = settlementRepo.findByOrderIdAndRetailerId(orderId, retailerId);
        if (existing.isPresent()) {
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
    public RetailerSettlement markEligible(String orderId) {
        RetailerSettlement s = settlementRepo.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found for order: " + orderId));
        s.markEligible();
        return settlementRepo.save(s);
    }

    @Transactional
    public RetailerSettlement markSettled(Long settlementId, String referenceNumber, String actor) {
        RetailerSettlement s = settlementRepo.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found with ID: " + settlementId));
        s.markSettled(referenceNumber);
        RetailerSettlement saved = settlementRepo.save(s);

        auditRepo.save(new RetailerAuditLog(
                "RetailerSettlement", String.valueOf(saved.getId()),
                "SETTLE_PAYMENT", actor, "Marked settlement as SETTLED with reference: " + referenceNumber
        ));

        return saved;
    }
}

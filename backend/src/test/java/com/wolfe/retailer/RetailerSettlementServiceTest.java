package com.wolfe.retailer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class RetailerSettlementServiceTest {
    private RetailerMarginRuleRepository marginRepo;
    private RetailerSettlementRepository settlementRepo;
    private RetailerRepository retailerRepo;
    private RetailerAuditLogRepository auditRepo;
    private RetailerSettlementAdjustmentRepository adjustmentRepo;
    private RetailerSettlementService service;

    @BeforeEach
    void setUp() {
        marginRepo = mock(RetailerMarginRuleRepository.class);
        settlementRepo = mock(RetailerSettlementRepository.class);
        retailerRepo = mock(RetailerRepository.class);
        auditRepo = mock(RetailerAuditLogRepository.class);
        adjustmentRepo = mock(RetailerSettlementAdjustmentRepository.class);
        service = new RetailerSettlementService(marginRepo, settlementRepo, retailerRepo, auditRepo, adjustmentRepo);
    }

    @Test
    void cannotSettlePendingSettlement() {
        RetailerSettlement settlement = new RetailerSettlement("WLF-1", 7L, 10000L, 1000L, 9000L);
        assertThrows(IllegalStateException.class, () -> settlement.markSettled("REF-1"));
    }

    @Test
    void testMarginCalculationWithCategoryRule() {
        RetailerMarginRule rule = new RetailerMarginRule(null, "Hardware", null, null, "PERCENTAGE", new BigDecimal("12.0"), 1);
        when(marginRepo.findMatchingRules(1L, 100L, 10L, "Hardware")).thenReturn(List.of(rule));

        long gross = 500000; // ₹5,000.00
        var calc = service.calculateLineItemSettlement(1L, "Hardware", 10L, 100L, gross);

        assertEquals(500000, calc.grossAmount());
        assertEquals(60000, calc.wolfeMarginAmount()); // 12% of 5,000 = 600
        assertEquals(440000, calc.retailerPayableAmount()); // 5,000 - 600 = 4,400
    }

    @Test
    void testMarginCalculationWithRetailerDefaultFallback() {
        when(marginRepo.findMatchingRules(any(), any(), any(), any())).thenReturn(List.of());

        Retailer r = new Retailer("Test Partner", "Owner", "test@retailer.com", "9999999999", "Address", "Jodhpur", "Rajasthan", "342001");
        r.setCommissionRate(new BigDecimal("15.0"));
        when(retailerRepo.findById(1L)).thenReturn(Optional.of(r));

        long gross = 100000; // ₹1,000.00
        var calc = service.calculateLineItemSettlement(1L, "Plywood", 10L, 100L, gross);

        assertEquals(100000, calc.grossAmount());
        assertEquals(15000, calc.wolfeMarginAmount()); // 15%
        assertEquals(85000, calc.retailerPayableAmount());
    }

    @Test
    void testSettlementLifecycle() {
        when(settlementRepo.findTopByOrderIdAndRetailerIdOrderByIdDesc("WLF-ORD-01", 1L)).thenReturn(Optional.empty());
        when(settlementRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        RetailerSettlement s = service.initializeSettlement("WLF-ORD-01", 1L, 100000, 10000, 90000);
        assertEquals("PENDING", s.getStatus());

        s.markEligible();
        assertEquals("ELIGIBLE", s.getStatus());

        s.markSettled("BANK-TXN-9988");
        assertEquals("SETTLED", s.getStatus());
        assertEquals("BANK-TXN-9988", s.getReferenceNumber());
    }
}

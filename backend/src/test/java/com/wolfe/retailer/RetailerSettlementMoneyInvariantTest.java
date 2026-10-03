package com.wolfe.retailer;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class RetailerSettlementMoneyInvariantTest {
    @Test
    void partialReturnsCanAccumulateBeforePayoutAndRemainEligible() {
        RetailerSettlement s = new RetailerSettlement("WLF-1", 7L, 10000L, 1000L, 9000L);
        s.markEligible();
        s.markAdjustedForReturn(2000L, "r1");
        assertEquals("ELIGIBLE", s.getStatus());
        assertEquals(2000L, s.getAdjustmentAmount());
        assertEquals(0L, s.getRecoveryDueAmount());
        s.markAdjustedForReturn(3000L, "r2");
        assertEquals("ELIGIBLE", s.getStatus());
        assertEquals(5000L, s.getAdjustmentAmount());
        assertEquals(0L, s.getRecoveryDueAmount());
        s.markSettled("BANK-1");
        assertEquals("SETTLED", s.getStatus());
    }

    @Test
    void returnAfterSettlementCreatesRecoveryInsteadOfSilentlyIgnoringIt() {
        RetailerSettlement s = new RetailerSettlement("WLF-2", 7L, 10000L, 1000L, 9000L);
        s.markEligible();
        s.markSettled("BANK-2");
        s.markAdjustedForReturn(2000L, "post-settlement return");
        assertEquals("RECOVERY_DUE", s.getStatus());
        assertEquals(2000L, s.getRecoveryDueAmount());
    }

    @Test
    void multipleReturnsAfterSettlementAccumulateRecovery() {
        RetailerSettlement s = new RetailerSettlement("WLF-3", 7L, 10000L, 1000L, 9000L);
        s.markEligible();
        s.markSettled("BANK-3");
        s.markAdjustedForReturn(2000L, "r1");
        s.markAdjustedForReturn(1500L, "r2");
        assertEquals("RECOVERY_DUE", s.getStatus());
        assertEquals(3500L, s.getRecoveryDueAmount());
    }
}

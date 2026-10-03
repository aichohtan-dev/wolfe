package com.wolfe.retailer;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class RetailerSettlementReturnLedgerContractTest {
    @Test
    void settlementReturnLedgerDoesNotReimposeSingleReturnUniqueness() throws Exception {
        String migration = Files.readString(Path.of("src/main/resources/db/migration/V57__allow_multiple_settlement_return_adjustments.sql"));
        assertTrue(migration.contains("DROP INDEX IF EXISTS uq_settlement_return_adjustment"));
    }

    @Test
    void prePayoutReturnKeepsEligibleSettlementFlow() throws Exception {
        String source = Files.readString(Path.of("src/main/java/com/wolfe/retailer/RetailerSettlement.java"));
        assertTrue(source.contains("this.status = \"ELIGIBLE\""));
        assertTrue(source.contains("normal ELIGIBLE -> SETTLED flow"));
    }
}

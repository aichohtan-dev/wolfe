package com.wolfe.retailer;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class RetailerSettlementConcurrencyContractTest {
    @Test
    void returnAndReassignmentAdjustmentsLockSettlementRows() throws Exception {
        String repo = Files.readString(Path.of("src/main/java/com/wolfe/retailer/RetailerSettlementRepository.java"));
        String service = Files.readString(Path.of("src/main/java/com/wolfe/retailer/RetailerSettlementService.java"));
        assertTrue(repo.contains("PESSIMISTIC_WRITE"));
        assertTrue(repo.contains("findByOrderIdAndRetailerIdForUpdateRows"));
        assertTrue(service.contains("findByOrderIdAndRetailerIdForUpdateRows(orderId, retailerId)"));
    }
}

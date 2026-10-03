package com.wolfe.returning;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ReturnProcessingLockOrderContractTest {
    @Test
    void centralReturnLocksAllVariantsBeforeGlobalInventory() throws Exception {
        String s = Files.readString(Path.of("src/main/java/com/wolfe/returning/ReturnProcessingService.java"));
        int variantMap = s.indexOf("Map<Long, Integer> selectedVariants = new java.util.TreeMap<>");
        int variantLock = s.indexOf("variants.findByIdForUpdate", variantMap);
        int inventoryLock = s.indexOf("globalInventory.findByProductIdForUpdate", variantLock);
        assertTrue(variantMap >= 0 && variantLock > variantMap && inventoryLock > variantLock);
    }

    @Test
    void retailerReturnProcessesRowsInDeterministicOrder() throws Exception {
        String s = Files.readString(Path.of("src/main/java/com/wolfe/returning/ReturnProcessingService.java"));
        int sorted = s.indexOf(".sorted(java.util.Comparator.comparing(OrderItem::getProductId)");
        int retailerLoop = s.indexOf("if (retailerId != null) {", sorted);
        int restock = s.indexOf("retailerInventory.restockStock", retailerLoop);
        assertTrue(sorted >= 0 && retailerLoop > sorted && restock > retailerLoop);
    }
}

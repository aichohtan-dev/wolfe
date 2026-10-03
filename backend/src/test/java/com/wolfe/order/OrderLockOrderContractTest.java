package com.wolfe.order;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;

class OrderLockOrderContractTest {
  @Test void cancellationUsesSameVariantThenInventoryOrderAsCreate() throws Exception {
    String s = Files.readString(Path.of("src/main/java/com/wolfe/order/OrderService.java"));
    int createVariant = s.indexOf("for (Long vid : variantQuantities.keySet())");
    int createInventory = s.indexOf("for (Long pid : inventoryQuantities.keySet())");
    int cancelVariant = s.indexOf("Map<Long, ProductVariant> lockedVariants");
    int cancelInventory = s.indexOf("for (var e : centralProductQuantities.entrySet())");
    assertTrue(createVariant >= 0 && createInventory > createVariant, "create must lock variants before inventory");
    assertTrue(cancelVariant >= 0 && cancelInventory > cancelVariant, "cancel must lock variants before inventory");
  }

  @Test void cancellationLocksMultipleVariantsDeterministically() throws Exception {
    String s = Files.readString(Path.of("src/main/java/com/wolfe/order/OrderService.java"));
    int treeMap = s.indexOf("Map<Long, Integer> centralVariantQuantities = new TreeMap<>()");
    int lockedLoop = s.indexOf("for (var e : centralVariantQuantities.entrySet())", treeMap);
    assertTrue(treeMap >= 0 && lockedLoop > treeMap, "variant locks must use deterministic TreeMap order");
  }
}

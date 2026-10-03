package com.wolfe.retailer;

import com.wolfe.inventory.Inventory;
import com.wolfe.inventory.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InventoryReconciliationService {
    public record Discrepancy(Long productId, int globalAvailable, int retailerAvailable, int delta) {}

    private final InventoryRepository global;
    private final RetailerInventoryRepository retailer;

    public InventoryReconciliationService(InventoryRepository global, RetailerInventoryRepository retailer) {
        this.global = global;
        this.retailer = retailer;
    }

    @Transactional(readOnly = true)
    public List<Discrepancy> findDiscrepancies() {
        Map<Long, Integer> retailerTotals = retailer.findAll().stream()
                .collect(Collectors.groupingBy(RetailerInventory::getProductId,
                        Collectors.summingInt(RetailerInventory::getAvailableStock)));
        return global.findAll().stream()
                .map(i -> discrepancy(i, retailerTotals.getOrDefault(i.getProductId(), 0)))
                .filter(d -> d.delta() != 0)
                .sorted(Comparator.comparing(Discrepancy::productId))
                .toList();
    }

    private Discrepancy discrepancy(Inventory i, int retailerAvailable) {
        int globalAvailable = i.getAvailable();
        return new Discrepancy(i.getProductId(), globalAvailable, retailerAvailable, globalAvailable - retailerAvailable);
    }
}

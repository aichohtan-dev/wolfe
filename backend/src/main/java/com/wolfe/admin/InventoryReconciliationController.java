package com.wolfe.admin;

import com.wolfe.retailer.InventoryReconciliationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/inventory")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class InventoryReconciliationController {
    private final InventoryReconciliationService service;
    public InventoryReconciliationController(InventoryReconciliationService service) { this.service = service; }

    @GetMapping("/reconciliation")
    public List<InventoryReconciliationService.Discrepancy> reconciliation() {
        return service.findDiscrepancies();
    }
}

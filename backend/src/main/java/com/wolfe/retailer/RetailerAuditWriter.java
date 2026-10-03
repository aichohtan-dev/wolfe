package com.wolfe.retailer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RetailerAuditWriter {
    private final RetailerAuditLogRepository repo;
    public RetailerAuditWriter(RetailerAuditLogRepository repo) { this.repo = repo; }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void allocationFailure(String orderId, RuntimeException ex) {
        repo.save(new RetailerAuditLog(
                "OrderAllocation", orderId, "AUTO_ALLOCATION_FAILED", "SYSTEM",
                "Allocation failed before completion: " + ex.getClass().getSimpleName()));
    }
}

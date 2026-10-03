package com.wolfe.retailer;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.OffsetDateTime;

@Component
public class RetailerAcceptanceSlaScheduler {
    private final RetailerAllocationService allocationService;
    private final com.wolfe.ops.SchedulerLockService schedulerLock;
    public RetailerAcceptanceSlaScheduler(RetailerAllocationService allocationService, com.wolfe.ops.SchedulerLockService schedulerLock) { this.allocationService = allocationService; this.schedulerLock = schedulerLock; }

    @Scheduled(fixedDelayString = "${WOLFE_RETAILER_ACCEPTANCE_SLA_MS:900000}")
    public void expireStaleAssignments() {
        schedulerLock.runIfLeader(0x574F4C4645534C41L, () -> {
            int minutes = Integer.parseInt(System.getenv().getOrDefault("WOLFE_RETAILER_ACCEPTANCE_SLA_MINUTES", "60"));
            allocationService.expireStaleAssignments(OffsetDateTime.now().minusMinutes(Math.max(5, minutes)));
            allocationService.retryUnassignedConfirmedOrders(50);
        });
    }
}

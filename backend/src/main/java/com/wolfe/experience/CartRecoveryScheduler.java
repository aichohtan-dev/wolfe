package com.wolfe.experience;

import java.time.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class CartRecoveryScheduler {
    private static final Logger log = LoggerFactory.getLogger(CartRecoveryScheduler.class);
    private final CartRecoveryClaimService claimer;
    private final com.wolfe.ops.SchedulerLockService schedulerLock;
    public CartRecoveryScheduler(CartRecoveryClaimService claimer, com.wolfe.ops.SchedulerLockService schedulerLock) {
        this.claimer = claimer;
        this.schedulerLock = schedulerLock;
    }
    @Scheduled(fixedDelayString = "PT1H")
    public void remindAbandonedCarts() {
        schedulerLock.runIfLeader(0x574F4C4645434152L, () -> remindAbandonedCartsLocked());
    }

    private void remindAbandonedCartsLocked() {
        Instant cutoff = Instant.now().minus(Duration.ofHours(24));
        while (claimer.sendNext(cutoff)) {
            // Each claim+notification is one transaction; continue until no candidates remain.
        }
    }
}

package com.wolfe.experience;

import com.wolfe.notification.NotificationService;
import java.time.*;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CartRecoveryScheduler {
    private final CartRecoveryRepository recovery;
    private final NotificationService notifications;
    public CartRecoveryScheduler(CartRecoveryRepository recovery, NotificationService notifications) {
        this.recovery = recovery;
        this.notifications = notifications;
    }
    @Scheduled(fixedDelayString = "PT1H")
    public void remindAbandonedCarts() {
        Instant cutoff = Instant.now().minus(Duration.ofHours(24));
        for (var cart:recovery.findByReminderSentFalseAndLastActivityBefore(cutoff)) {
            notifications.cartRecovery(cart.getCustomerId());
            cart.markReminder(UUID.randomUUID().toString());
            recovery.save(cart);
        }
    }
}

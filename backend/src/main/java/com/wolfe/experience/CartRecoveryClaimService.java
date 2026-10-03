package com.wolfe.experience;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import com.wolfe.notification.NotificationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartRecoveryClaimService {
    private final CartRecoveryRepository repo;
    private final NotificationService notifications;
    public CartRecoveryClaimService(CartRecoveryRepository repo, NotificationService notifications) { this.repo = repo; this.notifications = notifications; }
    @Transactional
    public Optional<CartRecovery> claimNext(Instant cutoff) {
        Instant now = Instant.now();
        var rows = repo.claimCandidates(cutoff, now.minus(java.time.Duration.ofMinutes(15)), PageRequest.of(0, 1));
        if (rows.isEmpty()) return Optional.empty();
        var cart = rows.get(0);
        cart.claimReminder(now);
        return Optional.of(repo.saveAndFlush(cart));
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public boolean sendNext(Instant cutoff) {
        Instant now = Instant.now();
        var rows = repo.claimCandidates(cutoff, now.minus(java.time.Duration.ofMinutes(15)), PageRequest.of(0, 1));
        if (rows.isEmpty()) return false;
        var cart = rows.get(0);
        cart.claimReminder(now);
        repo.saveAndFlush(cart);
        // Keep notification creation and claim completion in the same transaction.
        // If either database write fails, both roll back and the 15-minute claim lease
        // remains retryable instead of producing a duplicate notification later.
        notifications.cartRecovery(cart.getCustomerId());
        cart.markReminder(UUID.randomUUID().toString());
        repo.save(cart);
        return true;
    }
}

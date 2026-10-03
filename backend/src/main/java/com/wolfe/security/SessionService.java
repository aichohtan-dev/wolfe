package com.wolfe.security;

import com.wolfe.customer.Customer;
import com.wolfe.customer.CustomerRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessionService {
    private final RefreshTokenRepository repo;
    private final CustomerRepository customers;
    private final JwtService jwt;
    private final long refreshTtlSeconds;
    private final int maxSessions;
    private final SecureRandom random = new SecureRandom();
    public SessionService(RefreshTokenRepository repo, CustomerRepository customers, JwtService jwt,
    @Value("${WOLFE_REFRESH_TTL_SECONDS:2592000}") long refreshTtlSeconds,
    @Value("${WOLFE_MAX_SESSIONS:5}") int maxSessions) {
        this.repo = repo;
        this.customers = customers;
        this.jwt = jwt;
        this.refreshTtlSeconds = refreshTtlSeconds;
        this.maxSessions = Math.max(1, maxSessions);
    }
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 17 * * * *")
    @Transactional
    public void purgeExpiredAndRevokedTokens() {
        Instant now = Instant.now();
        repo.deleteByExpiresAtBefore(now);
        repo.deleteByRevokedAtBefore(now.minus(java.time.Duration.ofDays(7)));
    }

    @Transactional
    public Session issue(Customer c) { return issue(c, null); }
    @Transactional
    public Session issue(Customer c, String deviceLabel) {
        Customer locked = customers.findByIdForUpdate(c.getId()).orElseThrow(() -> new IllegalArgumentException("CUSTOMER_NOT_FOUND"));
        return new Session(jwt.issue(locked.getId(), locked.getEmail(), locked.getRole(), locked.getSessionVersion()), newRefresh(locked, deviceLabel));
    }
    @Transactional(noRollbackFor = InvalidRefreshTokenReuseException.class)
    public Session rotate(String raw) {
        if (raw == null || raw.isBlank()) throw new IllegalArgumentException("INVALID_REFRESH_TOKEN");
        RefreshToken old = repo.findByTokenHashForUpdate(hash(raw)).orElseThrow(() -> new IllegalArgumentException("INVALID_REFRESH_TOKEN"));
        // Serialize refresh-token lifecycle changes for the customer. Reuse detection must
        // lock the customer before revoking the token family; otherwise a concurrent login/
        // refresh can insert a new refresh token after the bulk revoke but before the session
        // version is advanced, leaving that newly-created token usable after reuse detection.
        Customer c = customers.findByIdForUpdate(old.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("INVALID_REFRESH_TOKEN"));
        if (!old.active()) {
            // A previously rotated/revoked token being presented is refresh-token reuse.
            // Customer locking makes family revocation and session-version invalidation atomic
            // with respect to concurrent session issuance.
            repo.revokeAllByCustomerId(old.getCustomerId(), Instant.now());
            c.incrementSessionVersion();
            customers.save(c);
            throw new InvalidRefreshTokenReuseException(old.getCustomerId());
        }
        if (!c.isEnabled() || c.isLocked()) throw new IllegalArgumentException("INVALID_REFRESH_TOKEN");
        old.revoke();
        repo.save(old);
        return issue(c);
    }
    @Transactional
    public void revokeOne(Long customerId, Long sessionId) {
        repo.findById(sessionId).filter(t -> t.getCustomerId().equals(customerId) && t.active()).ifPresent(t -> { t.revoke(); repo.save(t); });
    }

    public java.util.List<SessionInfo> listActive(Long customerId) {
        return repo.findByCustomerIdAndRevokedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(customerId, Instant.now()).stream()
                .map(t -> new SessionInfo(t.getId(), t.getCreatedAt(), t.getExpiresAt(), t.getDeviceLabel())).toList();
    }

    public record SessionInfo(Long id, Instant createdAt, Instant expiresAt, String device) {}

    @Transactional
    public void revokeAll(Long customerId) {
        repo.revokeAllByCustomerId(customerId, Instant.now());
    }

    @Transactional
    public void revoke(String raw) {
        if (raw == null || raw.isBlank()) return;
        repo.findByTokenHash(hash(raw)).ifPresent(t -> {
            t.revoke();
            repo.save(t);
            // Logout revokes only the presented refresh session. Other devices remain signed in
            // until their own session is revoked or the access token expires.
            customers.findById(t.getCustomerId()).ifPresent(c -> customers.save(c));
        });
    }
    private String newRefresh(Customer c, String deviceLabel) {
        String raw = randomToken();
        Instant now = Instant.now();
        var active = repo.findByCustomerIdAndRevokedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(c.getId(), now);
        if (active.size() >= maxSessions) {
            int revokeCount = active.size() - maxSessions + 1;
            for (int i = 0; i < revokeCount && i < active.size(); i++) { active.get(active.size() - 1 - i).revoke(); repo.save(active.get(active.size() - 1 - i)); }
        }
        repo.save(new RefreshToken(c.getId(), hash(raw), now.plusSeconds(refreshTtlSeconds), deviceLabel));
        return raw;
    }
    private String randomToken() {
        byte[] b = new byte[48];
        random.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }
    static String hash(String raw) {
        try {
            return hex(MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
    private static String hex(byte[] b) {
        StringBuilder s = new StringBuilder();
        for (byte x:b)s.append(String.format("%02x", x));
        return s.toString();
    }
    public static class InvalidRefreshTokenReuseException extends RuntimeException {
        private final Long customerId;
        public InvalidRefreshTokenReuseException(Long customerId) { super("INVALID_REFRESH_TOKEN_REUSE"); this.customerId=customerId; }
        public Long getCustomerId(){return customerId;}
    }

    public record Session(String accessToken, String refreshToken) {
    }
}

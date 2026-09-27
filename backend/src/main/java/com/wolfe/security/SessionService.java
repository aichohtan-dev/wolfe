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

@Service
public class SessionService {
    private final RefreshTokenRepository repo;
    private final CustomerRepository customers;
    private final JwtService jwt;
    private final long refreshTtlSeconds;
    private final SecureRandom random = new SecureRandom();
    public SessionService(RefreshTokenRepository repo, CustomerRepository customers, JwtService jwt,
    @Value("${WOLFE_REFRESH_TTL_SECONDS:2592000}") long refreshTtlSeconds) {
        this.repo = repo;
        this.customers = customers;
        this.jwt = jwt;
        this.refreshTtlSeconds = refreshTtlSeconds;
    }
    public Session issue(Customer c) {
        return new Session(jwt.issue(c.getId(), c.getEmail(), c.getRole()), newRefresh(c));
    }
    public Session rotate(String raw) {
        RefreshToken old = repo.findByTokenHash(hash(raw)).orElseThrow(() -> new IllegalArgumentException("INVALID_REFRESH_TOKEN"));
        if (!old.active()) throw new IllegalArgumentException("INVALID_REFRESH_TOKEN");
        Customer c = customers.findById(old.getCustomerId()).orElseThrow(() -> new IllegalArgumentException("INVALID_REFRESH_TOKEN"));
        old.revoke();
        repo.save(old);
        return issue(c);
    }
    public void revoke(String raw) {
        repo.findByTokenHash(hash(raw)).ifPresent(t -> {
            t.revoke(); repo.save(t);
        }
        );
    }
    private String newRefresh(Customer c) {
        String raw = randomToken();
        repo.save(new RefreshToken(c.getId(), hash(raw), Instant.now().plusSeconds(refreshTtlSeconds)));
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
    public record Session(String accessToken, String refreshToken) {
    }
}

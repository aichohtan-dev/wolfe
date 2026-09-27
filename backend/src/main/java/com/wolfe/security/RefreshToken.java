package com.wolfe.security;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_refresh_customer", columnList = "customer_id"), @Index(name = "idx_refresh_expires", columnList = "expires_at")
}
)
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "customer_id", nullable = false) private Long customerId;
    @Column(name = "token_hash", nullable = false, unique = true, length = 64) private String tokenHash;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    @Column(name = "revoked_at") private Instant revokedAt;
    protected RefreshToken() {
    }
    public RefreshToken(Long customerId, String tokenHash, Instant expiresAt) {
        this.customerId = customerId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }
    public Long getId() {
        return id;
    }
    public Long getCustomerId() {
        return customerId;
    }
    public String getTokenHash() {
        return tokenHash;
    }
    public Instant getExpiresAt() {
        return expiresAt;
    }
    public Instant getRevokedAt() {
        return revokedAt;
    }
    public void revoke() {
        this.revokedAt = Instant.now();
    }
    public boolean active() {
        return revokedAt == null && expiresAt.isAfter(Instant.now());
    }
}

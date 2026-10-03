package com.wolfe.customer;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "account_tokens", indexes = {
    @Index(name = "idx_account_token_customer_type", columnList = "customer_id,type"),
    @Index(name = "idx_account_token_expires", columnList = "expires_at")
})
public class AccountToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name="customer_id", nullable=false) private Long customerId;
    @Column(nullable=false, length=20) private String type;
    @Column(name="token_hash", nullable=false, unique=true, length=64) private String tokenHash;
    @Column(name="expires_at", nullable=false) private Instant expiresAt;
    @Column(name="used_at") private Instant usedAt;
    protected AccountToken() {}
    public AccountToken(Long customerId, String type, String tokenHash, Instant expiresAt) { this.customerId=customerId; this.type=type; this.tokenHash=tokenHash; this.expiresAt=expiresAt; }
    public Long getId(){return id;} public Long getCustomerId(){return customerId;} public String getType(){return type;} public String getTokenHash(){return tokenHash;} public Instant getExpiresAt(){return expiresAt;} public Instant getUsedAt(){return usedAt;}
    public boolean active(){return usedAt==null && expiresAt.isAfter(Instant.now());}
    public void use(){this.usedAt=Instant.now();}
}

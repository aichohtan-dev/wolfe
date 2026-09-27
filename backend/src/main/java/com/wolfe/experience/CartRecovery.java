package com.wolfe.experience;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "cart_recovery")
public class CartRecovery {
    @Id private Long customerId;
    @Column(nullable = false) private Instant lastActivity = Instant.now();
    @Column(nullable = false) private boolean reminderSent = false;
    @Column(length = 1000) private String recoveryToken;
    protected CartRecovery() {
    }
    public CartRecovery(Long customerId) {
        this.customerId = customerId;
    }
    public Long getCustomerId() {
        return customerId;
    }
    public Instant getLastActivity() {
        return lastActivity;
    }
    public boolean isReminderSent() {
        return reminderSent;
    }
    public String getRecoveryToken() {
        return recoveryToken;
    }
    public void touch() {
        lastActivity = Instant.now();
        reminderSent = false;
    }
    public void markReminder(String token) {
        reminderSent = true;
        recoveryToken = token;
    }
}

package com.wolfe.notification;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "customer_notifications")
public class CustomerNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "customer_id", nullable = false) private Long customerId;
    @Column(nullable = false, length = 40) private String type;
    @Column(nullable = false, length = 200) private String title;
    @Column(nullable = false, length = 1000) private String message;
    @Column(name = "reference_type", length = 40) private String referenceType;
    @Column(name = "reference_id", length = 100) private String referenceId;
    @Column(name = "read_at") private Instant readAt;
    @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();
    protected CustomerNotification() {
    }
    public CustomerNotification(Long customerId, String type, String title, String message, String referenceType, String referenceId) {
        this.customerId = customerId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
    }
    public Long getId() {
        return id;
    }
    public Long getCustomerId() {
        return customerId;
    }
    public String getType() {
        return type;
    }
    public String getTitle() {
        return title;
    }
    public String getMessage() {
        return message;
    }
    public String getReferenceType() {
        return referenceType;
    }
    public String getReferenceId() {
        return referenceId;
    }
    public Instant getReadAt() {
        return readAt;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public void markRead() {
        readAt = Instant.now();
    }
}

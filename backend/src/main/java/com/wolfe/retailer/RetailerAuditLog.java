package com.wolfe.retailer;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "retailer_audit_logs")
public class RetailerAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_name", nullable = false, length = 100)
    private String entityName;

    @Column(name = "entity_id", nullable = false, length = 100)
    private String entityId;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(nullable = false, length = 150)
    private String actor;

    @Column(length = 2000)
    private String details;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public RetailerAuditLog() {}

    public RetailerAuditLog(String entityName, String entityId, String action, String actor, String details) {
        this.entityName = entityName;
        this.entityId = entityId;
        this.action = action;
        this.actor = actor != null ? actor : "SYSTEM";
        this.details = details;
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public String getEntityName() { return entityName; }
    public String getEntityId() { return entityId; }
    public String getAction() { return action; }
    public String getActor() { return actor; }
    public String getDetails() { return details; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}

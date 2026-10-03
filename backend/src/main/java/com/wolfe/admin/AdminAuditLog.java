package com.wolfe.admin;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "admin_audit_logs", indexes = {
        @Index(name = "idx_admin_audit_created_at", columnList = "created_at"),
        @Index(name = "idx_admin_audit_actor", columnList = "actor_id")
})
public class AdminAuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name="actor_id") private Long actorId;
    @Column(nullable=false, length=120) private String actorEmail;
    @Column(nullable=false, length=12) private String method;
    @Column(nullable=false, length=500) private String path;
    @Column(nullable=false) private int statusCode;
    @Column(nullable=false, length=80) private String action;
    @Column(nullable=false) private Instant createdAt = Instant.now();

    protected AdminAuditLog() {}
    public AdminAuditLog(Long actorId, String actorEmail, String method, String path, int statusCode, String action) {
        this.actorId=actorId; this.actorEmail=actorEmail; this.method=method; this.path=path;
        this.statusCode=statusCode; this.action=action;
    }
    public Long getId(){return id;}
}

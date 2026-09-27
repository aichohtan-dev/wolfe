package com.wolfe.customdesign;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "custom_design_requests") public class CustomDesignRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    @Column(name = "customer_id", nullable = false) Long customerId;
    @Column(nullable = false) String projectName;
    @Column(length = 2000) String requirements;
    @Column(name = "reference_image_url", length = 2000) String referenceImageUrl;
    @Column(nullable = false) String status = "NEW";
    @Column(name = "created_at", nullable = false) Instant createdAt = Instant.now();
    protected CustomDesignRequest() {
    }
    public CustomDesignRequest(Long c, String p, String r, String u) {
        customerId = c;
        projectName = p;
        requirements = r;
        referenceImageUrl = u;
    }
    public Long getId() {
        return id;
    }
    public Long getCustomerId() {
        return customerId;
    }
    public String getProjectName() {
        return projectName;
    }
    public String getRequirements() {
        return requirements;
    }
    public String getReferenceImageUrl() {
        return referenceImageUrl;
    }
    public String getStatus() {
        return status;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public void status(String s) {
        status = s;
    }
}

package com.wolfe.consultation;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="consultation_requests", indexes={@Index(name="idx_consultation_status_created", columnList="status,created_at"), @Index(name="idx_consultation_email", columnList="email")})
public class ConsultationRequest {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Version private long version;
    @Column(nullable=false, length=100) private String name;
    @Column(nullable=false, length=150) private String email;
    @Column(length=160) private String project;
    @Column(nullable=false, length=1000) private String message;
    @Column(nullable=false, length=30) private String status="NEW";
    @Column(name="created_at", nullable=false) private Instant createdAt=Instant.now();
    protected ConsultationRequest() {}
    public ConsultationRequest(String name,String email,String project,String message){this.name=name;this.email=email;this.project=project;this.message=message;}
    public Long getId(){return id;} public String getName(){return name;} public String getEmail(){return email;} public String getProject(){return project;} public String getMessage(){return message;} public String getStatus(){return status;} public Instant getCreatedAt(){return createdAt;}
    public void status(String value){this.status=value;}
}

package com.wolfe.customer;

import jakarta.persistence.*;

@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true) private String email;
    @Column(nullable = false) private String name;
    @Column(nullable = false) private String passwordHash;
    @Column(nullable = false) private String role = "CUSTOMER";
    @Column(nullable = false) private boolean enabled = true;
    @Column(nullable = false) private boolean locked = false;
    @Column(name = "email_verified", nullable = false) private boolean emailVerified = true;
    @Column(name = "session_version", nullable = false) private long sessionVersion = 0;
    @Column(length = 30) private String phone;
    protected Customer() {
    }
    public Customer(String name, String email, String passwordHash) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }
    public Long getId() {
        return id;
    }
    public String getEmail() {
        return email;
    }
    public String getName() {
        return name;
    }
    public String getPasswordHash() {
        return passwordHash;
    }
    public void changePasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public boolean isEnabled() { return enabled; }
    public boolean isLocked() { return locked; }
    public boolean isEmailVerified() { return emailVerified; }
    public void markEmailVerified() { this.emailVerified = true; }
    public void markEmailUnverified() { this.emailVerified = false; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setLocked(boolean locked) { this.locked = locked; }
    public long getSessionVersion() { return sessionVersion; }
    public void incrementSessionVersion() { this.sessionVersion = Math.addExact(this.sessionVersion, 1); }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role == null || role.isBlank() ? "CUSTOMER" : role.trim().toUpperCase(); }
    public String getPhone() {
        return phone;
    }
    public void anonymize() {
        this.name = "Deleted Customer";
        this.email = "deleted+" + id + "+" + java.util.UUID.randomUUID() + "@invalid.wolfe";
        this.phone = null;
        this.passwordHash = "!DELETED!" + java.util.UUID.randomUUID();
        this.emailVerified = false;
    }
    public void updateProfile(String name, String phone) {
        this.name = name.trim();
        this.phone = phone == null || phone.isBlank()?null:phone.trim();
    }
}

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
    public String getRole() {
        return role;
    }
    public String getPhone() {
        return phone;
    }
    public void updateProfile(String name, String phone) {
        this.name = name.trim();
        this.phone = phone == null || phone.isBlank()?null:phone.trim();
    }
}

package com.wolfe.address;

import jakarta.persistence.*;

@Entity
@Table(name = "customer_addresses")
public class CustomerAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "customer_id", nullable = false) private Long customerId;
    @Column(nullable = false) private String label;
    @Column(nullable = false) private String recipientName;
    @Column(nullable = false, length = 30) private String phone;
    @Column(nullable = false, length = 1000) private String address;
    @Column(nullable = false) private String city;
    @Column(nullable = false) private String state;
    @Column(nullable = false, length = 20) private String pincode;
    @Column(nullable = false) private boolean isDefault;
    protected CustomerAddress() {
    }
    public CustomerAddress(Long customerId, String label, String recipientName, String phone, String address, String city, String state, String pincode,
    boolean isDefault) {
        this.customerId = customerId;
        this.label = label;
        this.recipientName = recipientName;
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
        this.isDefault = isDefault;
    }
    public Long getId() {
        return id;
    }
    public Long getCustomerId() {
        return customerId;
    }
    public String getLabel() {
        return label;
    }
    public String getRecipientName() {
        return recipientName;
    }
    public String getPhone() {
        return phone;
    }
    public String getAddress() {
        return address;
    }
    public String getCity() {
        return city;
    }
    public String getState() {
        return state;
    }
    public String getPincode() {
        return pincode;
    }
    public boolean isDefault() {
        return isDefault;
    }
    public void update(String label, String recipientName, String phone, String address, String city, String state, String pincode, boolean isDefault) {
        this.label = label.trim();
        this.recipientName = recipientName.trim();
        this.phone = phone.trim();
        this.address = address.trim();
        this.city = city.trim();
        this.state = state.trim();
        this.pincode = pincode.trim();
        this.isDefault = isDefault;
    }
}

package com.wolfe.retailer;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "retailers")
public class Retailer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private long version;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "owner_name", length = 150)
    private String ownerName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 50)
    private String phone;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String state = "Rajasthan";

    @Column(nullable = false, length = 20)
    private String pincode;

    @Column(name = "delivery_radius_km", nullable = false)
    private BigDecimal deliveryRadiusKm = new BigDecimal("25.0");

    @Column(precision = 9, scale = 6) private BigDecimal latitude;
    @Column(precision = 9, scale = 6) private BigDecimal longitude;

    @Column(nullable = false, length = 50)
    private String status = "ACTIVE"; // PENDING, ACTIVE, SUSPENDED, INACTIVE

    @Column(name = "verification_status", nullable = false, length = 50)
    private String verificationStatus = "VERIFIED"; // UNVERIFIED, VERIFIED

    @Column(name = "agreement_status", nullable = false, length = 50)
    private String agreementStatus = "SIGNED"; // PENDING, SIGNED

    @Column(nullable = false)
    private BigDecimal rating = new BigDecimal("4.8");

    @Column(name = "commission_rate", nullable = false)
    private BigDecimal commissionRate = new BigDecimal("10.0");

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public Retailer() {}

    public Retailer(String name, String ownerName, String email, String phone, String address, String city, String state, String pincode) {
        this.name = name;
        this.ownerName = ownerName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.state = state != null ? state : "Rajasthan";
        this.pincode = pincode;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; touch(); }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; touch(); }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; touch(); }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; touch(); }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; touch(); }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; touch(); }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; touch(); }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; touch(); }

    public BigDecimal getDeliveryRadiusKm() { return deliveryRadiusKm; }
    public void setDeliveryRadiusKm(BigDecimal deliveryRadiusKm) { this.deliveryRadiusKm = deliveryRadiusKm; touch(); }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setCoordinates(BigDecimal latitude, BigDecimal longitude) {
        if (latitude != null && (latitude.compareTo(new BigDecimal("-90")) < 0 || latitude.compareTo(new BigDecimal("90")) > 0)) throw new IllegalArgumentException("invalid retailer latitude");
        if (longitude != null && (longitude.compareTo(new BigDecimal("-180")) < 0 || longitude.compareTo(new BigDecimal("180")) > 0)) throw new IllegalArgumentException("invalid retailer longitude");
        this.latitude = latitude; this.longitude = longitude; touch();
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; touch(); }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; touch(); }

    public String getAgreementStatus() { return agreementStatus; }
    public void setAgreementStatus(String agreementStatus) { this.agreementStatus = agreementStatus; touch(); }

    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; touch(); }

    public BigDecimal getCommissionRate() { return commissionRate; }
    public void setCommissionRate(BigDecimal commissionRate) {
        if (commissionRate == null) throw new IllegalArgumentException("commission rate is required");
        if (commissionRate.signum() < 0 || commissionRate.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("commission rate must be between 0 and 100 percent");
        }
        this.commissionRate = commissionRate;
        touch();
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; touch(); }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status) && "VERIFIED".equalsIgnoreCase(this.verificationStatus);
    }

    private void touch() {
        this.updatedAt = OffsetDateTime.now();
    }
}

package com.wolfe.retailer;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "retailer_service_areas", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"retailer_id", "pincode"})
})
public class RetailerServiceArea {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "retailer_id", nullable = false)
    private Long retailerId;

    @Column(nullable = false, length = 20)
    private String pincode;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(name = "area_name", length = 150)
    private String areaName;

    @Column(name = "delivery_eta_hours", nullable = false)
    private int deliveryEtaHours = 24;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public RetailerServiceArea() {}

    public RetailerServiceArea(Long retailerId, String pincode, String city, String areaName, int deliveryEtaHours) {
        this.retailerId = retailerId;
        this.pincode = pincode;
        this.city = city;
        this.areaName = areaName;
        this.deliveryEtaHours = deliveryEtaHours;
        this.active = true;
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public Long getRetailerId() { return retailerId; }
    public void setRetailerId(Long retailerId) { this.retailerId = retailerId; }
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getAreaName() { return areaName; }
    public void setAreaName(String areaName) { this.areaName = areaName; }
    public int getDeliveryEtaHours() { return deliveryEtaHours; }
    public void setDeliveryEtaHours(int deliveryEtaHours) { this.deliveryEtaHours = deliveryEtaHours; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}

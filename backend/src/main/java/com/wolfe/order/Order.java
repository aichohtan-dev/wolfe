package com.wolfe.order;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "orders")
public class Order {
    @Id private String id;
    @Version private long version;
    @Column(name = "customer_id", nullable = false) private Long customerId;
    @Column(nullable = false) private String status;
    @Column(nullable = false) private long total;
    @Column(nullable = false) private long subtotal;
    @Column(name = "shipping_fee", nullable = false) private long shippingFee;
    @Column(name = "shipping_method", nullable = false) private String shippingMethod;
    @Column(name = "discount_amount", nullable = false) private long discountAmount;
    @Column(name = "coupon_code", length = 40) private String couponCode;
    @Column(nullable = false) private String currency;
    @Column(name = "payment_method", nullable = false) private String paymentMethod;
    @Column(name = "customer_name", nullable = false) private String customerName;
    @Column(name = "customer_email", nullable = false) private String customerEmail;
    @Column(name = "phone", nullable = false) private String phone;
    @Column(name = "address", nullable = false, length = 1000) private String address;
    @Column(name = "city", nullable = false) private String city;
    @Column(name = "pincode", nullable = false, length = 20) private String pincode;
    @Column(name = "idempotency_key", length = 100) private String idempotencyKey;
    @Column(name = "support_number", nullable = false, unique = true, length = 16) private String supportNumber;
    @Column(precision = 9, scale = 6) private java.math.BigDecimal latitude;
    @Column(precision = 9, scale = 6) private java.math.BigDecimal longitude;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    protected Order() {
    }
    public Order(String id, Long customerId, String status, long total, String currency, String paymentMethod, String shippingMethod, String customerName,
    String customerEmail, String phone, String address, String city,
    String pincode) {
        this.id = id;
        this.supportNumber = "WLF-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        this.customerId = customerId;
        this.status = status;
        this.total = total;
        this.subtotal = 0;
        this.shippingFee = 0;
        this.shippingMethod = shippingMethod;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.pincode = pincode;
        this.createdAt = Instant.now();
    }
    public void setAmounts(long subtotal, long shippingFee) {
        setAmounts(subtotal, shippingFee, 0, null);
    }
    public void setAmounts(long subtotal, long shippingFee, long discountAmount, String couponCode) {
        this.subtotal = subtotal;
        this.shippingFee = shippingFee;
        this.discountAmount = discountAmount;
        this.couponCode = couponCode;
        this.total = Math.addExact(Math.subtractExact(subtotal, discountAmount), shippingFee);
    }
    public String getId() {
        return id;
    }
    public Long getCustomerId() {
        return customerId;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public long getTotal() {
        return total;
    }
    public long getSubtotal() {
        return subtotal;
    }
    public long getShippingFee() {
        return shippingFee;
    }
    public long getDiscountAmount() {
        return discountAmount;
    }
    public String getCouponCode() {
        return couponCode;
    }
    public String getShippingMethod() {
        return shippingMethod;
    }
    public String getCurrency() {
        return currency;
    }
    public String getPaymentMethod() {
        return paymentMethod;
    }
    public String getCustomerName() {
        return customerName;
    }
    public String getCustomerEmail() {
        return customerEmail;
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
    public String getPincode() {
        return pincode;
    }
    public void setIdempotencyKey(String key) { this.idempotencyKey = key; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getSupportNumber() { return supportNumber; }
    public java.math.BigDecimal getLatitude() { return latitude; }
    public java.math.BigDecimal getLongitude() { return longitude; }
    public void setCoordinates(java.math.BigDecimal latitude, java.math.BigDecimal longitude) {
        if (latitude != null && (latitude.compareTo(new java.math.BigDecimal("-90")) < 0 || latitude.compareTo(new java.math.BigDecimal("90")) > 0)) throw new IllegalArgumentException("invalid order latitude");
        if (longitude != null && (longitude.compareTo(new java.math.BigDecimal("-180")) < 0 || longitude.compareTo(new java.math.BigDecimal("180")) > 0)) throw new IllegalArgumentException("invalid order longitude");
        this.latitude = latitude; this.longitude = longitude;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
}

package com.wolfe.discount;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "coupons") public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Version private long version;
    @Column(nullable = false, unique = true, length = 40) private String code;
    @Column(name = "discount_type", nullable = false, length = 12) private String discountType;
    @Column(nullable = false) private long value;
    @Column(name = "minimum_subtotal", nullable = false) private long minimumSubtotal;
    @Column(name = "maximum_discount", nullable = false) private long maximumDiscount;
    @Column(name = "usage_limit") private Integer usageLimit;
    @Column(name = "per_customer_usage_limit") private Integer perCustomerUsageLimit;
    @Column(name = "used_count", nullable = false) private int usedCount;
    @Column(nullable = false) private boolean active;
    @Column(name = "starts_at") private Instant startsAt;
    @Column(name = "expires_at") private Instant expiresAt;
    public Coupon() {
    }
    public Long getId() {
        return id;
    }
    public String getCode() {
        return code;
    }
    public String getDiscountType() {
        return discountType;
    }
    public long getValue() {
        return value;
    }
    public long getMinimumSubtotal() {
        return minimumSubtotal;
    }
    public long getMaximumDiscount() {
        return maximumDiscount;
    }
    public Integer getUsageLimit() { return usageLimit; }
    public Integer getPerCustomerUsageLimit() { return perCustomerUsageLimit; }
    public int getUsedCount() {
        return usedCount;
    }
    public boolean isActive() {
        return active;
    }
    public Instant getStartsAt() {
        return startsAt;
    }
    public Instant getExpiresAt() {
        return expiresAt;
    }
    public void configure(String code, String type, long value, long min, long max, Integer limit, boolean active, Instant starts, Instant expires) {
        configure(code, type, value, min, max, limit, null, active, starts, expires);
    }

    public void configure(String code, String type, long value, long min, long max, Integer limit, Integer perCustomerLimit, boolean active, Instant starts, Instant expires) {
        if (code == null || code.isBlank())throw new IllegalArgumentException("coupon code is required");
        String c = code.trim().toUpperCase();
        if (c.length()>40)throw new IllegalArgumentException("coupon code is too long");
        String t = type == null?"":type.trim().toUpperCase();
        if (!t.equals("PERCENT") && !t.equals("FIXED"))throw new IllegalArgumentException("discount type must be PERCENT or FIXED");
        if (value <= 0 || (t.equals("PERCENT") && value>100))throw new IllegalArgumentException("invalid discount value");
        if (min<0 || max<0 || (limit != null && limit<1) || (perCustomerLimit != null && perCustomerLimit<1))throw new IllegalArgumentException("invalid coupon limits");
        if (starts != null && expires != null && !expires.isAfter(starts))throw new IllegalArgumentException("expiry must be after start");
        this.code = c;
        this.discountType = t;
        this.value = value;
        this.minimumSubtotal = min;
        this.maximumDiscount = max;
        this.usageLimit = limit;
        this.perCustomerUsageLimit = perCustomerLimit;
        this.active = active;
        this.startsAt = starts;
        this.expiresAt = expires;
    }
    public void incrementUsage() {
        usedCount++;
    }
    public void decrementUsage() {
        if (usedCount > 0) usedCount--;
    }
}

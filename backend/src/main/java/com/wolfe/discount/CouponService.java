package com.wolfe.discount;

import java.time.Instant;
import org.springframework.stereotype.Service;

@Service public class CouponService {
    private final CouponRepository coupons;
    public CouponService(CouponRepository coupons) {
        this.coupons = coupons;
    }
    public Coupon require(String code, long subtotal) {
        if (code == null || code.isBlank())return null;
        Coupon c = coupons.findByCode(code.trim().toUpperCase()).orElseThrow(() -> new IllegalArgumentException("invalid coupon code"));
        validate(c, subtotal);
        return c;
    }
    public Coupon requireForOrder(String code, long subtotal) {
        if (code == null || code.isBlank())return null;
        Coupon c = coupons.findByCodeForUpdate(code.trim().toUpperCase()).orElseThrow(() -> new IllegalArgumentException("invalid coupon code"));
        validate(c, subtotal);
        return c;
    }
    public long discount(String code, long subtotal) {
        Coupon c = require(code, subtotal);
        if (c == null)return 0;
        return calculate(c, subtotal);
    }
    public long calculate(Coupon c, long subtotal) {
        long d = c.getDiscountType().equals("PERCENT")?Math.multiplyExact(subtotal, c.getValue())/100:c.getValue();
        if (c.getMaximumDiscount()>0)d = Math.min(d, c.getMaximumDiscount());
        return Math.min(d, subtotal);
    }
    private void validate(Coupon c, long subtotal) {
        Instant now = Instant.now();
        if (!c.isActive())throw new IllegalArgumentException("coupon is inactive");
        if (c.getStartsAt() != null && now.isBefore(c.getStartsAt()))throw new IllegalArgumentException("coupon is not active yet");
        if (c.getExpiresAt() != null && !now.isBefore(c.getExpiresAt()))throw new IllegalArgumentException("coupon has expired");
        if (subtotal<c.getMinimumSubtotal())throw new IllegalArgumentException("minimum order value for coupon is not met");
        if (c.getUsageLimit() != null && c.getUsedCount() >= c.getUsageLimit())throw new IllegalArgumentException("coupon usage limit reached");
    }
}

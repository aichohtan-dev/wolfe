package com.wolfe.discount;

import com.wolfe.order.OrderRepository;
import java.time.Instant;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service public class CouponService {
    private final CouponRepository coupons;
    private final OrderRepository orders;
    public CouponService(CouponRepository coupons, OrderRepository orders) {
        this.coupons = coupons;
        this.orders = orders;
    }
    public Coupon require(String code, long subtotal) {
        if (code == null || code.isBlank())return null;
        Coupon c = coupons.findByCode(code.trim().toUpperCase()).orElseThrow(() -> new IllegalArgumentException("invalid coupon code"));
        validate(c, subtotal);
        return c;
    }
    public Coupon requireForOrder(String code, long subtotal) {
        return requireForOrder(code, subtotal, null, 0);
    }

    public Coupon requireForOrder(String code, long subtotal, Long customerId, long priorCustomerUses) {
        if (code == null || code.isBlank())return null;
        Coupon c = coupons.findByCodeForUpdate(code.trim().toUpperCase()).orElseThrow(() -> new IllegalArgumentException("invalid coupon code"));
        validate(c, subtotal);
        long currentCustomerUses = customerId == null ? priorCustomerUses
                : orders.countByCustomerIdAndCouponCodeAndStatusNot(customerId, c.getCode(), "CANCELLED");
        if (c.getPerCustomerUsageLimit() != null && currentCustomerUses >= c.getPerCustomerUsageLimit())
            throw new IllegalArgumentException("coupon customer usage limit reached");
        return c;
    }
    public long discount(String code, long subtotal) {
        Coupon c = require(code, subtotal);
        if (c == null)return 0;
        return calculate(c, subtotal);
    }
    @org.springframework.transaction.annotation.Transactional
    public void releaseUsage(String code) {
        if (code == null || code.isBlank()) return;
        coupons.findByCodeForUpdate(code.trim().toUpperCase()).ifPresent(c -> {
            c.decrementUsage();
            coupons.save(c);
        });
    }

    public long calculate(Coupon c, long subtotal) {
        long d = c.getDiscountType().equals("PERCENT")
                ? BigDecimal.valueOf(subtotal).multiply(BigDecimal.valueOf(c.getValue()))
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN).longValueExact()
                : c.getValue();
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

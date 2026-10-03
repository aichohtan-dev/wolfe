# Wolfe Zero-Gap 2.0 — Batch 21

## Basis
Fresh review starts from `Wolfe-V49-zero-gap-2.0-batch20.zip`, with targeted reconciliation against the 107-numbered / 101-unique forensic findings supplied by the user.

The forensic ledger classified 24 findings as partially valid / hardening / severity-overstated. This batch rechecked the current source and closed three concrete residual items.

## Fixes

### F21-01 — Coupon percentage arithmetic overflow hardening (#17)
`CouponService.calculate()` previously used `Math.multiplyExact(subtotal, value) / 100` for percentage coupons. Although normal values are safe, the multiplication could overflow for extreme `long` subtotals before the percentage division.

**Fix:** use `BigDecimal` percentage arithmetic with explicit `RoundingMode.DOWN`, then exact conversion. This preserves the existing integer-paise semantics while eliminating intermediate `long` overflow.

### F21-02 — Configuration expiry was hard-coded (#86)
Configuration validity was hard-coded to 30 days in order checkout, cart validation, and experience configuration validation.

**Fix:** introduce `WOLFE_CONFIGURATION_TTL_DAYS` with a default of 30 days and use it consistently in all three paths. Default behavior is unchanged; production policy can now be configured without code changes.

### F21-03 — Refund status notification (#99)
The notification layer had return-update notifications but no dedicated refund-processed notification.

**Fix:** add `REFUND_UPDATE` notification generation after a return is successfully settled/restocked and its refund is already in `REFUNDED` state.

## Verification

Static/source checks:
- BigDecimal coupon calculation present: PASS
- Old `Math.multiplyExact(subtotal, couponValue)` percentage path absent: PASS
- `WOLFE_CONFIGURATION_TTL_DAYS` property present: PASS
- Order/cart/experience all use configurable TTL: PASS
- Refund notification service present and wired into return completion: PASS
- Source tree preserved without unrelated migration changes: PASS

Runtime boundary:
- Maven compile was attempted but could not start because Maven Wrapper could not resolve `repo.maven.apache.org` (DNS/network limitation).
- PostgreSQL/Redis/Docker/browser E2E remain runtime-unverified.

## Reconciliation impact

This batch closes three concrete items from the prior partial/hardening set:
- #17 coupon arithmetic overflow edge case
- #86 configuration expiry hard-coding
- #99 refund-status notification coverage

The remaining partial/hardening findings from the forensic ledger must continue to be assessed individually; design-only items are not automatically defects.

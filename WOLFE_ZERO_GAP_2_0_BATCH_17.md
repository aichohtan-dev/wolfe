# Wolfe Zero-Gap 2.0 — Batch 17

Base: Wolfe-V49-zero-gap-2.0-batch16.zip

## Confirmed finding fixed

### Shipping threshold was evaluated before coupon discount
`OrderService.create()` calculated `shippingFee(subtotal, ...)` before subtracting the coupon discount. The API contract explicitly described the threshold basis as the final discounted merchandise subtotal, so orders could receive free shipping even after a coupon reduced the merchandise subtotal below the threshold.

### Fix
Shipping eligibility now uses `subtotal - couponDiscount` and total is calculated from that same discounted merchandise subtotal plus shipping.

## Verification
- Source contract: shipping fee receives `discountedMerchandiseSubtotal` — PASS
- Total invariant: `total = discountedMerchandiseSubtotal + shippingFee` — PASS
- No migration changes — PASS
- Previous Zero-Gap 2.0 batch contracts preserved by source recheck.

## Runtime boundary
Maven compile/test was attempted but dependency download failed because `repo.maven.apache.org` could not be DNS-resolved in this environment. PostgreSQL/Redis/Docker/browser E2E remain runtime gates.

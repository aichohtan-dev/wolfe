# Wolfe V18.22 — Front/Back Cross-Module Alignment

## Fixed
- Fixed Checkout bundle-discount calculation using an out-of-scope `items` reference; Checkout now uses its `cart` input.
- Fixed CartDrawer bundle-discount calculation to use its `items` input.

## Admin UI parity added
- Added Commerce admin tab exposing coupon creation/disable and existing coupon data.
- Added Returns admin status control wired to `/admin/returns/{id}`.
- Added low-stock alert view.
- Added back-in-stock subscription visibility.
- Added cart-recovery visibility.
- Added Bundle Edit control wired to the existing backend update endpoint.

## Cross-module wiring
- Admin load now fetches coupons, returns, stock alerts, back-in-stock subscriptions and cart recovery records.
- Existing backend APIs remain authoritative for pricing and state transitions.
- Razorpay/payment implementation remains untouched.

## Verification
- App.tsx brace balance: PASS.
- App.tsx parenthesis balance: PASS.
- Checkout no longer references undefined `items`.
- CartDrawer uses its own `items` input.
- Backend endpoints for coupons, returns and bundle update confirmed present.
- Full npm/Maven runtime build remains uncertified because dependencies are not installed/network access is unavailable in the environment.

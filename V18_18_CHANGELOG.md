# Wolfe V18.18 — Configuration Price Snapshot & Route Cleanup

## Fixed
- Configured order pricing now uses the saved configuration's server-side `basePrice + addonPrice` snapshot instead of re-reading the current product price.
- Configured cart/checkout display now prefers the saved configuration `basePrice + addonPrice` snapshot.
- Removed duplicate `/compare` and `/config/:token` React routes.

## Verification
- Flyway migrations: 25 files / 25 unique versions / latest V26.
- Configuration price snapshot is used in order subtotal calculation.
- Configuration price snapshot is used in configured cart pricing.
- Duplicate route count: 1 each.
- Full npm install/build and Maven compile remain blocked by this environment's external DNS/dependency access; no runtime green claim is made.

## Payment
- Razorpay remains untouched/disabled. COD remains the active checkout method.

# Wolfe V18.8 — Commerce Shipping

- Added server-authoritative shipping calculation for checkout.
- Standard delivery: ₹199 below ₹2,500 subtotal; free at/above ₹2,500.
- Added persisted order subtotal, shipping fee, shipping method and recalculated total.
- Added shipping quote API for checkout estimates.
- Updated checkout UI to show delivery method, shipping charge and final total.
- Razorpay/payment gateway integration remains intentionally untouched.

## Verification

- Source sanity checks: PASS.
- Full frontend/backend runtime build: not certified in this environment because dependencies are unavailable locally.

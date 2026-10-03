# Wolfe v42 Final Security + Pricing Audit

## Scope

This pass used the integrated security-audit methodology as the review model and re-checked the v41 findings after applying fixes. The review covered authentication/session state, rate limiting, customer IDOR boundaries, retailer allocation/fulfillment, inventory locking, settlement/COD, returns, coupon concurrency, price/unit conversions, configuration pricing, PDF import, exception handling, browser URL sinks, and catalog fallbacks.

## Fixed in v42

1. Existing JWTs now fail for disabled/locked customers; refresh rotation also checks account state.
2. Login/register/coupon/consultation rate-limit subject buckets are bound to the client IP, with a separate per-IP ceiling; the old global-email login lockout is removed.
3. Cancellation remains order-row locked; coupon usage is released after the saved cancellation state.
4. Coupon per-customer usage is counted after the coupon row is locked, serializing concurrent orders for the same coupon.
5. Retailer reject is order + fulfillment locked and cannot race cancellation or started fulfillment.
6. Admin cannot directly mark an order DELIVERED unless the retailer fulfillment is already DELIVERED.
7. Fulfillment lookup is deterministic when historical rows exist.
8. Variant SKU is mandatory; missing legacy SKUs are backfilled in V33.
9. Order-line net amount and coupon discount are persisted in paise; settlement uses exact line_net_amount instead of truncated unit-price multiplication.
10. Configuration share responses recalculate current product/accessory prices, so old stored snapshots are not exposed as current pricing.
11. Return processing locks the return row and order, enforces a 30-day return window, validates positive/refundable amounts, restocks inventory, and applies a proportional retailer settlement adjustment.
12. COD settlement now has expected/collected/deposited cash reconciliation and settlement is blocked until reconciliation.
13. Settlement mutation uses row locks.
14. Review eligibility now requires a DELIVERED order.
15. API exception responses are null-safe and 4xx-specific; unexpected errors are logged without exposing internals.
16. Bundle listings no longer fail globally because one bundle has no active products; nullable image URLs are safe.
17. Public variant API no longer treats variant stockQuantity as the checkout authority; checkout/global inventory remains authoritative.
18. Demo/static catalog fallback was removed from the Shop failure path; API catalog is authoritative.
19. Product list default response was capped at 100; several admin full-table list endpoints were capped at 100.
20. Refresh-token cleanup is scheduled for expired tokens and old revoked tokens.

## Pricing audit result

Money boundaries are now explicit:

- Catalog product/variant/accessory prices: rupee BigDecimal.
- Conversion to order money: `movePointRight(2).longValueExact()`.
- Coupon fixed values, minimum/maximum discount, shipping, order totals, line-net amounts and settlement: integer paise.
- Percentage discounts are integer percentages and calculated in paise.
- Coupon discounts are allocated across order lines; randomised arithmetic checks over 100,000 cases preserved the exact order-level discount and line-net sum.
- Bundle discount allocation was checked over 100,000 random cases and reconciled exactly.
- Order detail displays persisted `lineNetAmount`, avoiding UI multiplication of a truncated per-unit value.

## Source verification

`final_static_audit.py`: PASS for 133 Java files and all targeted security invariants.

Frontend `tsc --noEmit` could not reach a clean result because the checkout environment has no installed React/npm dependencies. The first blocking errors are missing modules such as `react`, `react-router-dom`, and `lucide-react`, not a reported syntax error in the v42 changes.

Backend Maven compile could not run because Maven 3.9.11 is not cached and outbound DNS/network access is unavailable (`repo.maven.apache.org` could not be resolved). Docker is not installed in the audit environment.

Therefore this is a source/static audit result, not a runtime release certification.

## Remaining validation gates

- Run `./backend/mvnw test` in a network-enabled or fully cached environment.
- Run `npm ci && npm run build && npm run lint` in a dependency-enabled environment.
- Run the concurrent cancel/reject/deliver/settlement integration tests against PostgreSQL + Redis.
- Run the PDF 50 MB/import-review tests.
- Run the existing Cloudflare-style coverage ledger/verifier workflow after runtime dependencies are available.

---
**Certification note:** Historical/archival report. It is not runtime certification evidence. See `docs/REPORTING_POLICY.md` and the current Zero-Gap Master Findings ledger for authoritative certification status.

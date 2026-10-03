# Wolfe Zero-Gap 2.0 — Batch 23

## Scope
Final source-level closure pass over the remaining partial/hardening claims from the supplied 107-finding forensic ledger, starting from Batch 22.

## Concrete fixes

### F23-01 — Distributed global rate-limit ceiling (#47)
Added a Redis-backed global bucket per authentication action in `RateLimitService` (default 1,000 attempts / 15 minutes) in addition to subject/IP and IP buckets. Nginx also now applies a distributed-by-IP global API ceiling (`120r/s`, burst 240) so a coordinated multi-IP client cannot rely solely on the application subject/IP buckets.

### F23-02 — Remove repeated product lookups in recently-viewed API (#13/#19 adjacent performance hardening)
`ExperienceController.recent()` previously loaded each active product separately. It now performs one `findAllByIdInAndActiveTrue` query and reconstructs the original viewing order in memory.

### F23-03 — Frontend bundle-size governance (#80)
Added `scripts/bundle-size-check.mjs` and wired it into the production build. The default JavaScript bundle budget is 3,000,000 bytes and is configurable with `WOLFE_MAX_JS_BUNDLE_BYTES`.

## Reconciliation of the remaining partial claims

The remaining entries were individually rechecked rather than automatically labelled defects:

- #13/#19: the cited order-variant lazy-load concern is not a demonstrated current correctness/security bug; relevant recent-product repeated loading was nevertheless removed as additional hardening.
- #37: per-request customer lookup is intentional because session-version revocation is checked against current server state; replacing it with a cache would weaken revocation freshness.
- #40: CSRF is already enforced on refresh and all state-changing requests. Cryptographically binding the CSRF token to the refresh token would be defense-in-depth, not a demonstrated bypass.
- #44: Redis `noeviction` is an explicit fail-closed memory policy, not a missing policy.
- #47: global ceilings are now added at both application and proxy layers.
- #75: dynamic product pages remain client-rendered; known static routes are prerendered. Full per-product SSR would require a runtime rendering architecture, not a safe one-line bug fix.
- #78: frontend contract tests plus live-backend Maven tests exist; browser E2E remains a runtime-certification gate.
- #80: explicit bundle budget is now enforced in production builds.
- #84: authoritative stored line totals remain exact; the cited display rounding difference is not a stored-money integrity defect.
- #87: configurations are intentionally reusable and are always server-side re-priced at checkout; no unsafe price trust was found.
- #88: checkout already snapshots authoritative unit/line prices into `OrderItem`; a separate configuration-price audit trail is optional traceability, not a calculation defect.
- #93: page-size caps are an intentional abuse/DoS control. A future cursor API is scalability enhancement, not a correctness bug.
- #96: self-service email change is a product capability not present in the original scope; adding it requires a complete verification/change workflow. It is not silently treated as a security defect.

## Status

- Confirmed-open claims from the 107-finding forensic ledger: 0 at source level.
- Concrete residual hardening items fixed in this batch: 3.
- Runtime certification: still UNVERIFIED for PostgreSQL concurrency, Redis runtime behavior, Docker/Nginx runtime, browser E2E and production restore.

# Wolfe v40 — Security & Integrity Fix Report

## Scope
This release applies the requested follow-up fixes on top of v39, using the Wolfe Master PRD as the baseline.

## Fixed
- Configuration checkout now verifies customer ownership and re-prices current product/variant/accessory data server-side instead of trusting stored configuration price snapshots.
- Retailer fulfillment transitions lock the order and fulfillment rows to prevent concurrent double-delivery/stock deduction.
- Retailer order visibility excludes reassigned/cancelled assignments.
- Return completion now performs global inventory restock, retailer inventory restock, and settlement adjustment in a transactional path.
- Settlement remains state-guarded and now records return adjustment amount plus COD reconciliation fields.
- Coupon global usage can be released on cancellation; optional per-customer usage limit added to API/UI.
- Inventory reservation iteration is deterministic to reduce deadlock risk.
- Order references use full UUID entropy rather than 8 hex characters.
- Account enabled/locked flags added and enforced at login/JWT authentication.
- Coupon quote and consultation endpoints receive rate limiting with proxy-aware client IP extraction.
- Persistent customer cart is cleared after successful order placement; frontend cart merge deduplicates variant/configuration/bundle identities.
- Visual hero CTA URLs are protocol/relative-path allowlisted.
- Swagger/OpenAPI endpoints are no longer globally permit-all; OpenAPI remains disabled by default.
- PDF import is queued for background processing, upload limits are 50 MB, import image storage is persisted in Docker, and imported images are served safely.
- PDF approve-all handles REVIEW_REQUIRED/IMPORTED items and does not mark unresolved jobs COMPLETED.
- Unicode-only product names get safe generated slugs.
- Admin dashboard uses aggregate count queries instead of loading whole tables for its summary metrics.
- Bundle admin list avoids `Map.of` null-value crashes.
- New Flyway migration V32 adds integrity/account/return/COD/settlement constraints and fields without modifying historical migrations.
- HSTS was removed from the HTTP-only Nginx listener; HTTPS deployment should add HSTS at the TLS termination layer.

## Verification
- Source-level smoke assertions: PASS.
- Java brace/structure smoke: PASS.
- Docker Compose config: attempted in the build environment.
- Maven compile/tests: NOT EXECUTED successfully because the Maven wrapper requires downloading Maven 3.9.11 and DNS/network access is unavailable in this environment.
- Frontend typecheck/build: NOT EXECUTED because `node_modules` is absent and network access is unavailable.

## Important deployment verification
Before production rollout, run:
- `backend/mvnw test`
- `npm ci --ignore-scripts`
- `npm run build`
- Docker Compose build/start/health verification
- Flyway migration against a production-like PostgreSQL copy
- Concurrency integration tests for cancel, retailer delivery, reassignment and return completion

No git commit or push was performed.

---
**Certification note:** Historical/archival report. It is not runtime certification evidence. See `docs/REPORTING_POLICY.md` and the current Zero-Gap Master Findings ledger for authoritative certification status.

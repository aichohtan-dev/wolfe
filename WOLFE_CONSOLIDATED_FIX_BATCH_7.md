# Wolfe Consolidated Fix Batch 7

## Changes
- CORS now supports a comma-separated trusted-origin allowlist via `WOLFE_FRONTEND_ORIGINS`, while retaining the legacy single-origin variable.
- Order creation DTO adds explicit size bounds for customer name, email, city, payment/shipping methods and product slug.
- CI adds a lightweight credential-pattern scan.
- Runtime certification wording explicitly separates static/source evidence from unavailable runtime execution.
- Master 001-100 ledger updated: #42 and #46 are partial/yellow rather than red.
- Master status counts were recomputed from all 100 individual rows; stale prior summary counts are explicitly superseded.

## Verification boundary
Static/source checks only. Maven, PostgreSQL, Redis, Docker and browser/E2E remain NOT VERIFIED in this environment.

---
**Certification note:** Historical/archival report; not runtime certification evidence. See `docs/REPORTING_POLICY.md` and the current Zero-Gap Master Findings ledger for authoritative status.

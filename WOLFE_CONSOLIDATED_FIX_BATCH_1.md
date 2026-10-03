# Wolfe Consolidated Fix Batch 1 — Findings 001–100

Date: 2026-10-01
Source of truth: WOLFE_ZERO_GAP_FINDINGS_001_100_MASTER.md

## Scope fixed in this batch

### Priority findings
- 56: order slug lookup aligned with cart using case-insensitive trimmed lookup.
- 60: cart exact-line uniqueness retained; database integrity conflicts now return HTTP 409 instead of generic 500.
- 62: inactive products can be removed from carts.
- 74: cancellation releases coupon usage before inventory/variant locks, matching create lock order.
- 75: allocation failure audit moved to a separate REQUIRES_NEW Spring bean so the audit survives an outer transaction rollback.
- 78: retailer acceptance SLA scheduler expires stale ASSIGNED allocations and releases retailer reservations.
- 93: V43 integrity preflight script added without rewriting an existing Flyway migration checksum.

### Additional fixes
- Search LIKE metacharacters escaped; price range validation added; deterministic pagination tie-breakers added.
- Public catalog endpoints use explicit ProductPublicView DTOs.
- Product media URL validation and bounded DTO fields added.
- Cart configuration expiry enforced.
- Quote product existence/active validation added.
- Custom-design reference image URL restricted to HTTPS.
- Configuration accessory identity checked against config JSON.
- Cart recovery scheduler logs failures.
- Password UTF-8 byte length capped at BCrypt's 72-byte boundary.
- SameSite changed from Strict to Lax for normal top-level navigation compatibility.
- Security CSP adds object-src 'none'; COOP/CORP response headers added.
- PDF job API no longer exposes server filePath; job list is capped.
- PDF executor queue increased to reduce immediate rejection; runtime saturation still requires load testing.
- DB pool/Tomcat/request timeout configuration added.
- Compose restart, read-only, cap-drop, no-new-privileges and log rotation hardening added.
- PostgreSQL backup/restore scripts hardened for permissions, portable SHA-256, exit-on-error and single-transaction restore.
- Media backup/restore scripts added.
- index.html now declares lang="en".
- Frontend cached user object persistence is gated on explicit storage consent.

## Verification

PASS:
- 100/100 numbered findings remain present in the master ledger.
- YAML parsing: docker-compose.yml and application.yml.
- Shell syntax: all ops/*.sh scripts.
- Merge-marker scan.
- Priority invariant source checks.

NOT VERIFIED:
- Maven compile/tests: Maven 3.9.11 download blocked by repo.maven.apache.org DNS in the audit environment.
- Frontend typecheck/build: dependencies are not installed in the audit environment; the observed tsc errors are dependency/type-environment failures, not accepted as a source PASS.
- Docker/PostgreSQL/Redis/browser E2E.

## Explicitly not marked fixed
Business/architecture/runtime-dependent items remain red/yellow in the master ledger, including:
- settlement clawback/refund gateway workflow;
- damaged-return inspection disposition;
- global vs retailer inventory reconciliation;
- delivery-radius allocation semantics;
- password reset/email verification/account deletion;
- granular ADMIN/SUPER_ADMIN permissions;
- full observability/tracing;
- CI security/test expansion;
- runtime deadlock/load/restart/browser verification;
- SEO domain/prerender decisions;
- historical V14 migration intent.

---
**Certification note:** Historical/archival report; not runtime certification evidence. See `docs/REPORTING_POLICY.md` and the current Zero-Gap Master Findings ledger for authoritative status.

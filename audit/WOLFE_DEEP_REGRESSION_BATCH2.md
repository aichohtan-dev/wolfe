# Wolfe Deep Regression Audit — Batch 2

## Scope
Fresh static re-audit after Deep Security Batch-1 fixes.

## Confirmed regressions found and fixed

1. Refresh-token reuse revoked refresh sessions but did not invalidate already-issued access JWTs.
   - Fix: increment Customer.sessionVersion on refresh-token reuse.

2. Configuration share token generation used a UUID containing hyphens while the public share endpoint accepted only `[A-Za-z0-9]{16,64}`.
   - Fix: use the full UUID entropy without hyphens (32 hex characters, 128-bit UUID entropy preserved).

## Rechecked controls

- Invalid/stale access cookie no longer directly emits 401 from JwtAuthFilter; authorization rules decide anonymous vs protected behavior.
- Retailer authentication is linked only by customer/user ID; email fallback removed.
- Retailer API matcher is RETAILER-only, matching controller behavior.
- Bundle duplicate product rows aggregate with Integer::sum and whole-unit validation is enforced.
- Duplicate identical order lines merge before pricing/discount allocation.
- Configuration JSON is allowlisted and public share is time-limited.
- Quote and custom-design creation have rate limiting.
- Review purchase eligibility uses a repository existence query.
- Actuator info is admin-only.
- CSRF remains enabled.
- HTTP-only Nginx architecture does not emit HSTS; API SecurityConfig also does not emit HSTS.

## Runtime limitation

Maven compilation was attempted but Maven distribution/dependencies could not be resolved because `repo.maven.apache.org` DNS is unavailable in the audit environment. No runtime/build PASS is claimed.

---
**Certification note:** Historical/archival report; not runtime certification evidence. See `docs/REPORTING_POLICY.md` and the current Zero-Gap Master Findings ledger for authoritative status.

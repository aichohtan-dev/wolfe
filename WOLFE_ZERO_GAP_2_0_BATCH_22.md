# Wolfe Zero-Gap 2.0 — Batch 22

## Scope
Independent recheck of the second-AI 107-finding ledger, focusing on the remaining partial/hardening claims after Batch 21.

## Finding fixed

### #86 — Configuration expiry hard-coded 30 days (partial/hardening)

The customer-facing configuration TTL had been made configurable in Batch 21, but the anonymous-configuration cleanup scheduler still used a literal `Duration.ofDays(30)`. That created two possible retention policies when `WOLFE_CONFIGURATION_TTL_DAYS` was changed.

### Fix
`AnonymousConfigurationCleanupScheduler` now injects `WOLFE_CONFIGURATION_TTL_DAYS` (default 30), validates the configured range, and uses the same value for anonymous cleanup.

A source contract test was added to prevent regression to a hard-coded 30-day cleanup.

## Verification
- Config key alignment: PASS
- Hard-coded 30-day cleanup removed: PASS
- Default remains 30 days: PASS
- Constructor validation range: PASS
- Regression contract test added: PASS

## Runtime boundary
This batch is source/config/test-contract verified. Full Maven/PostgreSQL/Redis/Docker/browser runtime certification remains unverified in this environment.

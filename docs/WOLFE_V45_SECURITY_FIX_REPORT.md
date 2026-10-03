# Wolfe v45 Security Fix Report

Fixed the seven findings from the v44 deep hardening audit.

1. Removed duplicate Flyway V33 version by moving pricing/auth migration to V36.
2. Added V37 to drop the historical global settlement uniqueness and enforce one active (non-ADJUSTED) settlement per order/retailer, allowing A -> B -> A history safely. Reassignment now always targets the latest settlement.
3. Fixed retailer rejection/cancellation idempotency: rejected/reassigned/completed assignments are never released a second time; reassignment accepts the FAILED_DELIVERY fulfillment state.
4. Added a database-locked maximum of 3 active COD orders per customer (CONFIRMED/PROCESSING/SHIPPED) plus an index.
5. Updated stale retailer security tests for current constructor dependencies and added a rejection/cancellation regression test.
6. CI Docker services now use immutable Postgres/Redis manifest digests.
7. GitHub Actions are pinned to full commit SHAs and the policy workflow rejects mutable action tags.

Verification performed: duplicate migration scan, source/static assertions, YAML/policy checks, Java test-source consistency checks, ZIP integrity. Full Maven execution is attempted separately when Maven/dependencies are available.

---
**Certification note:** Historical/archival report. It is not runtime certification evidence. See `docs/REPORTING_POLICY.md` and the current Zero-Gap Master Findings ledger for authoritative certification status.

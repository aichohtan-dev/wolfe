# Wolfe Consolidated Fix Batch 9

## Closed / advanced findings

- **#40 — GREEN (static):** All four scheduled jobs now use a PostgreSQL `pg_try_advisory_xact_lock` through `SchedulerLockService`, preventing duplicate execution across application replicas.
- **#50 — YELLOW:** COD active-order limits now apply independently to customer account, normalized phone, and normalized address. Limits are configurable with `WOLFE_MAX_ACTIVE_COD_ORDERS_PER_PHONE` and `WOLFE_MAX_ACTIVE_COD_ORDERS_PER_ADDRESS`. OTP/strong identity verification remains pending.

## Verification
- 100-entry master ledger preserved.
- Changed Java files brace-balanced.
- All four scheduled jobs reference the distributed scheduler lock.
- OrderRepository contains phone/address active-COD count methods.
- Full Maven/PostgreSQL/Redis/Docker/browser runtime remains NOT VERIFIED in this environment.

## Remaining red findings
18, 19, 21, 30, 43, 79, 80, 81

---
**Certification note:** Historical/archival report; not runtime certification evidence. See `docs/REPORTING_POLICY.md` and the current Zero-Gap Master Findings ledger for authoritative status.

# Wolfe Zero-Gap 2.0 — Batch 16

## Baseline
`Wolfe-V49-zero-gap-2.0-batch15.zip`

## Audit focus
Independent source recheck focused on cross-module financial reporting, settlement lifecycle states, session/token concurrency, scheduler recovery, and previously fixed invariants.

## Finding
### G16-001 — Retailer dashboard earnings overstated after settlement adjustments
**Severity:** Medium / financial reporting integrity

`RetailerController.me()` previously summed `retailerPayableAmount` for both `SETTLED` and `ELIGIBLE` settlements. After a pre-payout return adjustment, `retailerPayableAmount` remains the original payable and the actual remaining eligible amount is `retailerPayableAmount - adjustmentAmount`. After settlement, the authoritative amount is `settledAmount`, which records the actual net payout.

This could make the retailer dashboard report earnings higher than the amount actually payable/paid, even though the underlying settlement ledger remained correct.

### Fix
- `SETTLED` uses `settledAmount`.
- `ELIGIBLE` uses `max(0, retailerPayableAmount - adjustmentAmount)`.
- `RECOVERY_DUE`, `ADJUSTED`, and other non-payable states contribute zero.

## Regression checks
- Settlement model still records `settledAmount` at payout.
- Pre-payout return adjustment remains cumulative.
- Recovery-due settlements are excluded from payable earnings.
- Reassignment-adjusted historical settlements are excluded.
- Refresh-token customer locking remains present.
- PDF recovery row re-lock and optimistic version remain present.
- Cart recovery claim/notification remains one transaction.
- COD phone/address advisory lock order remains deterministic.
- Java brace check passed for edited controller.

## Runtime boundary
PostgreSQL/Redis/Docker/browser runtime, true concurrent transaction execution, crash/restart tests, and load/deadlock tests remain unverified in this environment.

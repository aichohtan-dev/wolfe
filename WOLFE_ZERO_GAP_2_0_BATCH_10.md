# Wolfe Zero-Gap 2.0 — Batch 10

Base: Wolfe-V49-zero-gap-2.0-batch9.zip

## Findings fixed

1. **Settlement eligibility lost-update race** — `markEligible()` used a non-locking latest-settlement query while settlement payout/return adjustment paths use pessimistic row locks. A concurrent payout could be overwritten back to `ELIGIBLE`. It now locks the current settlement row before transition.

2. **Recovery collection stranded settlements** — `markRecoveryCollected()` changed `RECOVERY_DUE` to `ADJUSTED`, but `markSettled()` accepts only `ELIGIBLE`, so a collected recovery could never progress to payout. Recovery collection now returns the settlement to `ELIGIBLE`.

3. **Cumulative refund ceiling across multiple returns** — each return validated its selected items independently, but repeated HALF_UP per-line allocations could make cumulative paid refunds exceed the order total. `maxRefundableAmount()` now subtracts already-REFUNDED prior returns before allowing another refund.

## Verification
- Settlement `markEligible` row-lock contract: PASS
- Recovery `RECOVERY_DUE -> ELIGIBLE -> SETTLED` contract: PASS
- Cumulative prior-refund ceiling: PASS
- Previous Batch 6–9 settlement/return contracts: PASS by source assertions
- Migration history unchanged in this batch
- Runtime PostgreSQL/Redis/Maven/browser E2E remain unverified in this environment

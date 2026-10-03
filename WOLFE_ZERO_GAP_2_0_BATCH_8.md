# Wolfe Zero-Gap 2.0 — Batch 8

## Scope
Independent recheck of Batch 7 with emphasis on settlement money invariants, return-ledger concurrency, migration/schema consistency, async transaction boundaries, and previously fixed state-machine contracts.

## Confirmed findings fixed

### 1. Retailer settlement payout could ignore pre-payout return adjustments
`RetailerSettlement.markAdjustedForReturn()` correctly reduced `adjustmentAmount`, but `markSettled()` previously stored only the payout reference/status and had no persisted amount. The settlement therefore had no authoritative record of the net amount actually payable after adjustments.

**Fix:** added `settled_amount`; `markSettled()` records `max(retailerPayableAmount - adjustmentAmount, 0)` and refuses settlement when recovery remains due. Existing SETTLED rows are backfilled from the historical payable/adjustment values.

### 2. Return adjustment ledger could record more than the amount actually applied
`RetailerSettlementService.markAdjustedForReturn()` created the ledger row before the settlement object calculated the remaining available payable. On a later partial return, the ledger could therefore record an adjustment that was capped to zero by the settlement itself.

**Fix:** `markAdjustedForReturn()` returns the actual applied/recovery amounts; the ledger records only the actual applied amount.

### 3. V57 migration intent needed an explicit forward repair for deployments carrying the old index
The multiple-return fix must remove `uq_settlement_return_adjustment`. Batch 8 keeps historical migrations immutable and adds V59 as a forward-only defensive migration that drops the obsolete unique index if it exists.

## Rechecked contracts
- Order create/cancel lock order: PASS
- Retailer acceptance lock order: PASS
- Return central inventory lock order: PASS
- Retailer SLA stale candidate re-lock: PASS
- Account-token one-time-use locking: PASS
- Password 72-byte alignment: PASS
- PDF worker starts after transaction commit: PASS
- Multiple return ledger rows: PASS
- Settlement recovery state: PASS
- Settlement payout requires ELIGIBLE and records net amount: PASS
- Historical migration files V1-V58 not modified by this batch; V59 is forward-only.

## Runtime boundary
Maven/PostgreSQL/Redis/Docker/browser concurrency and E2E execution remain unverified in this environment. Static/source-level PASS is not runtime certification.

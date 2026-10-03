# Wolfe Zero-Gap 2.0 — Batch 9

## Scope
Independent recheck of Batch 8 with emphasis on settlement/return/reassignment financial invariants and fail-closed behavior.

## Confirmed finding fixed

### Settlement adjustment could silently no-op when settlement data was missing
`RetailerSettlementService.markAdjustedForReturn()` and `markAdjustedForReassignment()` previously used `ifPresent(...)`. If an order had retailer fulfillment but its settlement row was missing/corrupt, the business operation could complete without a corresponding settlement adjustment, leaving a financial reconciliation gap that would be difficult to detect.

### Fix
Both paths now:
- lock/read the latest settlement row with `findByOrderIdAndRetailerIdForUpdateRows()`;
- require a settlement row with `orElseThrow(...)`;
- fail the transaction rather than silently continuing when settlement accounting is missing.

## Invariants rechecked
- retailer return cannot complete settlement adjustment silently;
- retailer reassignment cannot silently lose settlement history;
- latest settlement row remains selected by `ORDER BY id DESC`;
- multiple historical adjusted settlements remain compatible with the active partial unique index;
- previous return/settlement money fixes remain intact;
- migration history unchanged in this batch.

## Verification
- Java source brace checks: PASS
- targeted source contract checks: PASS
- migration count/history unchanged: PASS
- Maven/runtime/PostgreSQL/Redis/Docker/browser E2E: not certified in this environment.

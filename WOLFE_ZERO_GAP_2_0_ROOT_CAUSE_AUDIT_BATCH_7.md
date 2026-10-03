# Wolfe Zero-Gap 2.0 — Root-Cause Audit / Batch 7

Date: 2026-10-02
Baseline: `Wolfe-V49-zero-gap-2.0-batch6.zip`
Method: state-machine + invariant + concurrency + transaction-boundary + migration-forensic recheck

## Confirmed defects found and fixed

### B7-01 — Settlement RETURN ledger still had a single-return unique index
V34 created `uq_settlement_return_adjustment (settlement_id, type)` for `type='RETURN'`, while Batch 6 changed the application model to record each legitimate partial return as a separate adjustment event. A second completed return would therefore hit a database uniqueness violation. Added forward-only V57 to drop the historical unique index; V34 remains immutable.

### B7-02 — Pre-payout return could strand an eligible retailer settlement
`RetailerSettlement.markAdjustedForReturn()` changed a pre-payout settlement to `ADJUSTED`, while `markSettled()` accepts only `ELIGIBLE`. Because returns can be completed after delivery (and retailer delivery marks the settlement eligible), a valid return before payout could make the settlement impossible to settle. The adjustment is now accumulated positively and the settlement remains `ELIGIBLE` when no recovery is due.

### B7-03 — PDF import worker could start before its database transaction committed
`PdfImportService.uploadAndProcess()` persisted the queued job and immediately submitted the worker from inside the transaction. The worker could query before the job row was committed and see no job. Worker submission is now registered with `afterCommit`; queue rejection marks the committed job failed and cleans the staging file.

### B7-04 — Admin global-stock update was not atomic with stock-alert notification/deactivation
The admin inventory update performed inventory save, notification creation, and subscription deactivation without an enclosing service transaction. A later failure could leave a notification committed while the subscription remained active, causing duplicate alerts. The endpoint is now transactional so these DB changes commit or roll back together.

## Verification contracts
- settlement pre-payout return remains ELIGIBLE and can SETTLE: PASS
- post-settlement returns accumulate recovery: PASS
- V57 removes obsolete single-return uniqueness: PASS
- PDF worker is afterCommit only: PASS
- admin inventory update is transactional: PASS
- previous Batch 6 lock/invariant contracts preserved: PASS

## Runtime boundary
Maven/PostgreSQL/Redis/Docker/browser/concurrent runtime execution remains unverified in this environment. Static evidence is not runtime certification.

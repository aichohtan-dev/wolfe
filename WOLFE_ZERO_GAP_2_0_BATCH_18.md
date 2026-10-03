# Wolfe V49 — Zero-Gap 2.0 — Batch 18

## Baseline
- Input: `Wolfe-V49-zero-gap-2.0-batch17.zip`
- Scope: independent deep recheck after Batch 17, with focus on return-data integrity and migration compatibility.
- Runtime boundary: Maven/Docker/PostgreSQL/Redis/browser runtime was not available in this environment.

## Finding fixed
### Legacy return rows could have no item-level quantity snapshot
`V35__return_item_quantities.sql` added `return_requests.item_quantities_json`, but did not backfill rows that already existed before V35. The application later uses this field to enforce cumulative per-item return limits.

For an old return with NULL/blank JSON, a subsequent customer return could fail while parsing the prior return data, and the system would not have the historical item quantities needed to enforce the remaining returnable quantity for that order.

## Fix
Added:
`backend/src/main/resources/db/migration/V61__backfill_return_item_quantities.sql`

The migration populates missing/blank `item_quantities_json` from the authoritative `order_items` rows, keyed by `order_item.id` with the ordered quantity. Existing non-empty selections are left unchanged.

## Verification
- Migration numbering checked: V61 follows existing V60.
- SQL source reviewed for idempotent NULL/blank targeting.
- Existing V35 behavior is preserved; this is a data-repair migration only.
- No application Java source changed in this batch.
- Runtime migration execution is unverified because PostgreSQL/Docker is unavailable here.
- Maven compile/runtime certification remains unverified because the environment cannot resolve Maven Central.

## Result
Batch 18 closes a concrete historical-data compatibility gap in the return-item quantity ledger.

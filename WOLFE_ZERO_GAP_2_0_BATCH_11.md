# Wolfe Zero-Gap 2.0 — Batch 11

## Base
Wolfe-V49-zero-gap-2.0-batch10.zip

## Confirmed finding fixed

### G11-01 — PDF import stale-job recovery can kill a healthy long-running worker

**Severity:** High reliability / data-integrity risk

`PdfImportRecoveryScheduler` marks `PROCESSING`/`EXTRACTING` PDF jobs as FAILED after one hour based only on `updated_at`, and deletes the staging PDF. `PdfImportService.processPdfFile()` previously updated `updated_at` only at phase transitions, not while processing pages. A legitimate large/slow PDF can therefore be running for more than one hour while appearing stale; the recovery scheduler can mark it FAILED and delete the file while the worker is still using it.

### Fix
- Added `PdfImportJob.heartbeat()`.
- `processPdfFile()` now refreshes `updated_at` approximately every 5 minutes during page processing.
- Recovery remains able to recover genuinely interrupted workers while a healthy long-running worker continuously refreshes its liveness timestamp.
- No historical Flyway migration changed.

## Regression checks
- heartbeat method present: PASS
- page-processing heartbeat interval present: PASS
- recovery cutoff remains 1 hour: PASS
- worker starts after transaction commit: PASS
- previous Batch 6–10 invariant contracts preserved by source inspection: PASS

## Runtime boundary
Maven/PostgreSQL/Redis/Docker/browser runtime remains unverified in this environment because Maven Central DNS resolution is unavailable.

# Wolfe Zero-Gap 2.0 — Batch 12

Base: Wolfe-V49-zero-gap-2.0-batch11.zip
Audit mode: independent root-cause / state / concurrency / async recovery recheck

## Confirmed findings fixed

### 1. PDF recovery scheduler could fail a healthy worker on a stale candidate snapshot
The recovery query produced a candidate list based on `updated_at`, then failed those entities without re-reading them. A worker heartbeat could race with that candidate snapshot.

Fix:
- Re-read each candidate under `PESSIMISTIC_WRITE` immediately before failing it.
- Re-check active status and current `updatedAt` against the cutoff.

### 2. A stale PDF worker entity could resurrect a job after recovery marked it FAILED
The worker and recovery scheduler could hold different JPA entity versions. Without optimistic versioning, a later worker save could overwrite the scheduler's FAILED state.

Fix:
- Added `@Version` to `PdfImportJob`.
- Added forward-only `V60__pdf_import_job_optimistic_version.sql`.
- Optimistic-lock failures are treated as loss of worker ownership rather than attempting a stale FAILED save.

### 3. PDF worker could change a recovered FAILED QUEUED job back to PROCESSING
The async worker previously loaded the job and unconditionally set PROCESSING. If recovery had already failed the queued job, the worker could resurrect it.

Fix:
- Added atomic `claimQueuedForProcessing()` update: only QUEUED jobs can be claimed.

### 4. Cross-customer COD phone/address caps were not concurrency-safe
Customer row locking serialized orders for the same customer, but phone/address limits span multiple customers. Two different customers using the same phone/address could both pass the last available slot concurrently.

Fix:
- Added transaction-scoped PostgreSQL advisory locks for normalized COD phone and address keys.
- Lock order is deterministic: customer → phone → address.
- No schema migration required.

## Verification

- PdfImportJob braces: PASS
- PdfImportJobRepository braces: PASS
- PdfImportRecoveryScheduler braces: PASS
- PdfImportService braces: PASS
- OrderService braces: PASS
- SchedulerLockService braces: PASS
- Scheduler stale-candidate re-lock: PASS
- Pdf optimistic version: PASS
- Atomic QUEUED claim: PASS
- Optimistic-lock failure fail-closed handling: PASS
- COD phone/address advisory locks: PASS
- Lock order deterministic: PASS
- Historical migrations unchanged; only new V60 added

## Runtime boundary

Maven/PostgreSQL/Redis/Docker/browser concurrency and failure-injection tests remain runtime certification gates. Static source checks do not replace those tests.

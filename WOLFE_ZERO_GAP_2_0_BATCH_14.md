# Wolfe Zero-Gap 2.0 — Batch 14

## Scope
Independent recheck of Batch 13, focusing on scheduler transaction boundaries, async retry/idempotency, token lifecycle, and fail-recovery paths.

## Findings fixed

### 1. Cart recovery scheduler transaction scope — HIGH
`SchedulerLockService.runIfLeader()` holds the advisory-lock transaction while the scheduler loops over every cart recovery. `sendNext()` previously joined that outer transaction, so a large backlog could keep one database transaction and advisory lock open across many notifications. A late failure could roll back the entire batch rather than one cart attempt.

**Fix:** `CartRecoveryClaimService.sendNext()` now uses `REQUIRES_NEW`, so each claim + notification + completion is one independent transaction while the leader advisory lock remains held only for scheduler leadership.

### 2. Expired account tokens were never purged — MEDIUM
`AccountTokenRepository` exposed `deleteByExpiresAtBefore`, but no scheduled job invoked it. Password-reset and email-verification token rows could accumulate indefinitely.

**Fix:** added a six-hour scheduled purge in `AccountLifecycleService`.

## Regression contracts
- cart claim + notification + completion remains atomic: PASS
- each cart recovery iteration is independently transactional: PASS
- scheduler leader lock remains exclusive: PASS
- expired account tokens have periodic cleanup: PASS
- password-reset one-time token locking preserved: PASS
- email-verification one-time token locking preserved: PASS
- PDF recovery/heartbeat/afterCommit contracts preserved: PASS
- Batch 6–13 state/invariant contracts preserved: PASS
- no Flyway migration modified or added: PASS

## Runtime boundary
PostgreSQL/Redis/Docker/browser runtime, real scheduler concurrency, crash/restart testing, and load testing remain unverified in this environment.

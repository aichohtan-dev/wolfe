# Wolfe Zero-Gap 2.0 — Batch 15

## Scope
Independent recheck of Batch 14, focused on session/refresh-token concurrency, scheduler/async recovery, idempotency, and transaction boundaries.

## Confirmed finding fixed

### 1. Refresh-token reuse vs concurrent session issuance race
`SessionService.rotate()` detected reuse and bulk-revoked the customer's refresh tokens before locking the customer row. A concurrent session issuance could therefore race with family revocation and create a new refresh token after the bulk revoke but before session-version invalidation.

### Fix
The rotate path now locks the customer (`findByIdForUpdate`) immediately after locking the presented refresh-token row. Reuse handling then performs family revocation and session-version increment while the customer is locked. Normal rotation uses the same customer lock before issuing a replacement token.

This establishes the refresh lifecycle ordering:

`RefreshToken(row) -> Customer(row) -> RefreshToken family changes`

and serializes reuse containment with concurrent session issuance.

## Rechecked and passing
- Refresh token row lock
- Customer lock before refresh issuance
- Session max-cap enforcement under customer lock
- Reuse `noRollbackFor` behavior
- Account-token customer -> token lock ordering
- Account-token scheduled cleanup
- Cart recovery per-item `REQUIRES_NEW` transaction
- Cart recovery claim lock
- PDF worker atomic QUEUED -> PROCESSING claim
- PDF optimistic versioning and heartbeat
- PDF stale-job recovery re-lock
- Order idempotency database unique index
- COD phone/address advisory-lock ordering

## Migration
No migration added or modified in Batch 15.

## Runtime boundary
Static/source verification only. PostgreSQL/Redis/Docker/browser concurrency and failure-injection runtime evidence remain required for final certification.

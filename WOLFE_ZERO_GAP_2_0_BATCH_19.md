# Wolfe Zero-Gap 2.0 — Batch 19

## Baseline
- Input artifact: `Wolfe-V49-zero-gap-2.0-batch18.zip`
- Audit mode: independent source-level recheck; prior reports were treated as evidence, not as proof.

## Finding G19-01 — Customer security/session state could be lost by concurrent profile/access writes

### Severity
High — concurrency/security integrity

### Root cause
`Customer` does not use a JPA `@Version` field. Several mutating endpoints loaded a customer with ordinary `findById()` and then saved the full entity. A concurrent update could therefore overwrite another transaction's security-sensitive state, including `passwordHash`, `sessionVersion`, `enabled`, `locked`, or role.

The highest-risk example was password change/logout/admin access update racing with a profile/access update. Without row locking or optimistic versioning, the later full-row write could restore stale `sessionVersion` or password/security state.

### Fix
Changed security/customer mutation paths to acquire the customer row with the existing pessimistic-write repository method `findByIdForUpdate()` before mutation:

- customer logout/session-version invalidation
- customer password change
- customer account deletion/anonymization
- customer profile update
- admin customer access/role update

This reuses the project's existing customer lock primitive and avoids introducing a migration or changing JWT/session semantics.

## Static verification
- `CustomerController.java`: all identified customer mutation paths use `findByIdForUpdate()` — PASS
- `AdminController.java` customer access mutation uses `findByIdForUpdate()` — PASS
- `CustomerRepository.findByIdForUpdate()` is `PESSIMISTIC_WRITE` — PASS
- no Customer schema migration added — PASS
- ZIP/source integrity — PASS

## Runtime boundary
Maven/PostgreSQL/Redis/Docker/browser runtime certification remains UNVERIFIED in this environment. The source-level fix does not claim runtime certification.

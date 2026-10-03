# Wolfe Zero-Gap 2.0 — Batch 20

## Scope

Independent recheck started from `Wolfe-V49-zero-gap-2.0-batch19.zip`, with the explicit goal of reconciling the second-AI forensic ledger `WOLFE_BATCH5_101_FORENSIC_VERIFICATION.md` and closing its five remaining confirmed gaps after #1 was already fixed.

The source ledger states that the PDF had 107 numbered mentions representing 101 unique claims, with six explicit duplicate entries (#102–#107). It identified six confirmed unresolved gaps at that checkpoint: #1, #20, #24, #41, #62 and #63.

## Batch 20 fixes

### #20 — Unassigned retailer allocation retry/reconciliation

**Fixed at source level.**

Added a scheduled reconciliation path for confirmed orders that have no active retailer fulfillment. The scheduler now:

1. expires stale ASSIGNED retailer reservations;
2. scans confirmed orders that have no active assignment/fulfillment;
3. re-runs automatic retailer allocation for up to 50 orders per scheduler cycle.

This closes the specific gap where automatic allocation could return false and leave a confirmed order indefinitely unassigned without a general retry path.

### #24 — Order status history actor attribution

**Fixed at source level.**

`order_status_history` now has an `actor` column via `V62__order_status_history_actor.sql`.

New history entries identify the actor/source, including customer, admin, and system/retailer-allocation transitions. Legacy rows remain nullable and are exposed as `SYSTEM` by the entity accessor.

### #41 — Security-event lifecycle coverage

**Fixed at source level.**

`SecurityEventLogger` now records successful login, successful refresh, and logout events in addition to denied requests and refresh-token reuse. Customer IDs are included where authenticated identity is known.

### #62 — Database statement timeout

**Fixed/configured at source level.**

PostgreSQL JDBC connections now set a configurable statement timeout:

`DB_STATEMENT_TIMEOUT_MS` (default 30,000 ms)

through Hikari's PostgreSQL `options=-c statement_timeout=...` connection property.

This is distinct from Hikari connection acquisition timeout and limits execution of long-running PostgreSQL statements.

### #63 — Long-running request timeout hardening

**Mitigated at source/edge level.**

Added:
- Spring MVC async request timeout: `WOLFE_ASYNC_REQUEST_TIMEOUT_MS` (default 30s)
- Nginx API `proxy_connect_timeout 5s`
- Nginx API `proxy_send_timeout 30s`
- Nginx API `proxy_read_timeout 30s`

This provides an explicit request/edge deadline for async handling and proxied API responses. It does **not** claim that a synchronous servlet handler is forcibly interrupted at 30 seconds; backend cancellation remains a runtime/architecture concern and is therefore not marked as fully runtime-certified.

## Additional reconciliation

The previously identified #1 order create/cancel lock-order issue remains fixed: variant locks are acquired before central product-inventory locks on the cancellation path, matching order creation.

## Verification

Static/source checks performed:

- Changed Java files have balanced braces: PASS
- V62 migration present and numbered after V61: PASS
- Order history actor field/getter/constructor: PASS
- Admin/customer/system actor propagation: PASS
- Unassigned-order retry repository query present: PASS
- Scheduler invokes retry reconciliation: PASS
- Security lifecycle event methods and call sites: PASS
- PostgreSQL statement timeout configuration present: PASS
- Spring async request timeout present: PASS
- Nginx API timeout directives present: PASS

### Runtime boundary

Maven compile was attempted but could not start because the environment could not resolve `repo.maven.apache.org` while Maven Wrapper attempted to download Maven 3.9.11 (`curl: (6) Could not resolve host`). PostgreSQL/Redis/Docker/browser E2E runtime behavior remains unverified.

## Finding ledger impact

At the Batch-5 forensic checkpoint, the six confirmed unresolved claims were #1, #20, #24, #41, #62 and #63. Batch 20 addresses #20, #24, #41, #62 and #63 at source/config level; #1 had already been fixed in the preceding batches.

Therefore, **0 of those six remain as unaddressed source-level gaps**, while runtime certification is still a separate final gate.

# Wolfe Zero-Gap 2.0 — Root-Cause Audit / Batch 6

Date: 2026-10-02
Baseline: `Wolfe-V49-fresh-100-recheck-fix-batch5.zip`
Method: state-machine + invariant + lock-order + lifecycle + security-source audit

## Objective

This audit intentionally changes method. Instead of only checking individual files/findings, it audits cross-module invariants and state transitions that can create bugs after an apparently correct local fix.

The audit does **not** claim mathematical proof that no future defect can ever exist. Runtime/PostgreSQL/Redis/Docker/browser execution remains a separate evidence gate. The target of this batch is: no known unresolved source-level defect in the audited root-cause areas.

## Confirmed defects found and fixed

### B6-01 — Order create/cancel lock inversion (CRITICAL)
- `OrderService.create()` locks variants then central inventory.
- `OrderService.transition(CANCELLED)` previously locked central inventory then variants.
- This created a classic cross-transaction cycle.
- Fix: cancellation now locks all variants in deterministic `TreeMap` order before any central inventory row.
- Regression contract updated in `OrderLockOrderContractTest`.

### B6-02 — Retailer acceptance lock inversion
- Retailer acceptance previously released central product inventory before restoring variant stock.
- Order creation/cancellation use variant -> inventory ordering.
- Mixed base-product/variant orders could therefore create an opposite lock order.
- Fix: retailer acceptance now locks all variants first, then all central product inventory rows, in deterministic order.
- Regression contract added.

### B6-03 — Return processing lock-order inversion
- Return settlement previously locked central variant/product resources item-by-item in repository order.
- A multi-line return could therefore interleave variant and inventory locks differently from order creation.
- Retailer return restocking could also acquire multiple retailer inventory rows in nondeterministic order.
- Fix: central returns aggregate and lock all variants first, then products; retailer returns process deterministic product/variant/item order.
- Regression contract added.

### B6-04 — Stale retailer SLA candidate could double-release retailer stock
- `expireStaleAssignments()` starts from a stale candidate query.
- A retailer could reject an assignment after the scheduler's candidate query but before the scheduler acquired the order lock.
- The scheduler previously trusted the stale `ASSIGNED` object and could release the same retailer reservation a second time.
- Fix: after acquiring the order lock, the scheduler re-reads and pessimistically locks the assignment and proceeds only if it is still `ASSIGNED` and still past the cutoff.
- Regression contract added.

### B6-05 — Concurrent account-token consumption race
- Verification/password-reset token lookup was previously non-locking.
- Two concurrent requests could both observe the same active one-time token before either marked it used.
- Password reset was especially sensitive because both requests could change the password.
- Fix: token confirmation now establishes a deterministic customer -> token lock order and re-reads the token with `PESSIMISTIC_WRITE` before consuming it.
- Token issuance also locks the customer before replacing existing tokens.
- Regression contract added.

### B6-06 — Password DTO limit inconsistent with BCrypt byte ceiling
- Login and change-password DTOs allowed 128 characters while runtime validation enforced 72 UTF-8 bytes.
- This is not an authentication bypass, but it was an API validation inconsistency.
- Fix: login/change-password DTO max changed to 72; the UTF-8 byte validator remains authoritative.
- Regression contract added.

### B6-07 — Retailer return settlement could lose legitimate partial-return adjustments
- Settlement adjustment logic treated the first adjustment as the only `RETURN` adjustment and did not correctly model returns after payout settlement.
- Multiple legitimate partial returns could therefore be omitted from settlement adjustment processing.
- A return after `SETTLED` could fail to create a recovery obligation.
- Fix: each completed return becomes its own adjustment event; settlement rows are pessimistically locked; cumulative recovery is maintained; `SETTLED`/`RECOVERY_DUE` returns create recovery rather than silently doing nothing.
- Regression tests added for multiple pre-settlement returns, post-settlement recovery, and cumulative recovery.

## Root-cause invariants now enforced in source

### Inventory lock invariant
All central stock mutation paths audited in this batch use:

`VARIANT locks (sorted) -> PRODUCT INVENTORY locks (sorted)`

where both authorities are present.

### Retailer inventory invariant
Multi-line retailer inventory mutations use deterministic item ordering, while each row is protected by a pessimistic lock.

### One-time token invariant
`customer row -> token row` is the global account-token lock order. Token consumption is row-locked before `used_at` is written.

### Retailer SLA invariant
A scheduler candidate is never trusted after the initial query. The current assignment state is re-read under lock before releasing stock.

### Settlement invariant
Each completed return is a separate financial event. Settlement state and recovery amount are updated under a pessimistic settlement lock.

## Existing external 101-finding report reconciliation

The external PDF was not treated as authoritative. It contains 107 numbered mentions with six duplicate entries and several findings that contradict current source. The strongest new issue it exposed was the create/cancel deadlock, which is fixed in this batch.

The external report remains useful as a threat-model input, not as a final finding count.

## Static verification performed

PASS:
- create variant -> inventory lock order
- cancellation variant -> inventory lock order
- retailer acceptance variant -> inventory lock order
- stale SLA assignment re-lock requirement
- central return variant -> inventory lock order
- deterministic retailer return ordering
- account token pessimistic lock
- customer -> token lock ordering
- password DTO 72-byte policy alignment
- settlement row pessimistic lock
- multiple settlement return support
- post-settlement recovery behavior
- no bare `orElseThrow()` in backend main source

Migration history: **55 migrations, unchanged from Batch 5; no historical migration modified or added in Batch 6.**

## Runtime boundary

Maven execution was attempted but the environment could not resolve `repo.maven.apache.org` (DNS failure). Therefore a green Maven test result is not claimed here.

Still required before final certification:
- PostgreSQL integration tests
- Redis integration tests
- true concurrent order/create/cancel/accept/return tests
- Docker + Nginx production stack
- browser E2E
- backup/restore drill
- load/deadlock test

## Next certification rule

No new batch should be called "final certification" until these runtime gates produce evidence. Future static audits should begin from this state-machine/invariant ledger rather than restarting from raw finding lists.

# Wolfe Zero-Gap 2.0 — State / Invariant Matrix

## 1. Order lifecycle

CONFIRMED -> PROCESSING -> SHIPPED -> DELIVERED
CONFIRMED -> CANCELLED
PROCESSING -> CANCELLED

Invariant: terminal order states cannot be mutated through a normal transition.

## 2. Central stock lifecycle

Create: reserve/decrement central authority.
Cancel before retailer acceptance: release central authority.
Retailer acceptance: transfer authority by releasing central reservation/restoring variant global stock.
Central delivery: fulfill central product inventory; variant stock remains consumed.
Central return: restore central product inventory or variant stock.

Lock invariant: variants first, product inventory second.

## 3. Retailer fulfillment lifecycle

ASSIGNED -> ACCEPTED -> PACKED -> READY_FOR_DELIVERY -> OUT_FOR_DELIVERY -> DELIVERED
ASSIGNED -> REJECTED/EXPIRED
FAILED_DELIVERY/REASSIGNED -> new assignment

Invariant: central stock is transferred exactly once at acceptance; retailer stock is released exactly once on pre-acceptance rejection/expiry/cancellation; delivery consumes retailer stock only.

## 4. Return lifecycle

PENDING -> APPROVED -> RECEIVED -> COMPLETED
PENDING -> REJECTED
APPROVED -> REJECTED

Invariant: cumulative returned quantity per order item cannot exceed ordered quantity.

## 5. Refund/settlement lifecycle

Return must be COMPLETED + REFUNDED before inventory settlement.

Invariant:
- each return is an independent financial event;
- a settled payout cannot be silently reduced;
- post-settlement return creates recovery due;
- duplicate processing of the same return is idempotent.

## 6. Authentication/session lifecycle

Issue -> active refresh token -> rotate -> revoke old token -> issue new token.

Invariant: refresh token reuse revokes the token family/session version; account reset/verification tokens are one-time and row-locked on consumption.

## 7. Concurrency matrix

Critical pairs to runtime-test:
- create x create
- create x cancel
- create x retailer accept
- create x return
- accept x cancel
- reject x SLA expiry
- reject x cancel
- reassignment x cancellation
- return x settlement
- return x refund completion
- refresh x refresh
- password reset x password reset

## 8. Failure-injection matrix

- DB failure after central reservation
- DB failure during retailer stock transfer
- response timeout after order commit
- duplicate order request
- duplicate return completion request
- duplicate refresh request
- Redis unavailable during authentication rate limiting
- mail provider failure during account token issuance

## 9. Certification rule

A state is not certified merely because one endpoint works. Every transition must preserve the applicable inventory, money, authorization, idempotency and audit invariants under both sequential and concurrent execution.

# Wolfe Fresh 100 Recheck — Batch 4

## Base
`Wolfe-V49-fresh-100-recheck-fix-batch3.zip`

## Scope
Independent source-level recheck of the retailer stock-authority lifecycle, cancellation/reassignment, and central-return inventory restoration after Batch 3.

## Confirmed findings fixed

### 1. Central-stock reservation state was inferred from only the latest fulfillment row
**Severity:** High

`isGlobalStockStillReserved()` looked only at the newest fulfillment status. After a retailer had accepted an order, a later `FAILED_DELIVERY`, `CANCELLED`, or `REASSIGNED` row could make the method report that central stock was still reserved even though central stock had already been released at acceptance.

**Impact:** cancellation/transition logic could release or fulfill central stock incorrectly after a historical retailer acceptance.

**Fix:** inspect fulfillment history and treat central stock as released once any fulfillment for the order reached `ACCEPTED`, `PACKED`, `READY_FOR_DELIVERY`, `OUT_FOR_DELIVERY`, or `DELIVERED`. Central stock remains considered reserved only when no such acceptance-or-beyond event exists.

### 2. Retailer rejection was allowed after acceptance
**Severity:** High

`rejectOrder()` accepted both `ASSIGNED` and `ACCEPTED` assignments. Acceptance is the point at which stock authority transfers from central inventory to the retailer, so allowing the same rejection path afterward could release retailer stock while leaving the order in an ambiguous central-stock state.

**Impact:** inventory authority could become inconsistent and the order could enter an unsafe reassignment path.

**Fix:** retailer rejection is now allowed only from `ASSIGNED`. Post-acceptance failures must use the explicit fulfillment/reassignment lifecycle rather than the pre-acceptance rejection operation.

### 3. Central fulfillment returns did not restore variant stock
**Severity:** High

Central orders decrement `ProductVariant.stockQuantity` at order creation. Return completion previously restored only the product-level `Inventory` quantity. For a central-only returned variant, the variant's stock remained permanently decremented.

**Impact:** returned variant inventory became unavailable for future sales and the catalog stock authority drifted from physical inventory.

**Fix:** `ReturnProcessingService.complete()` now locks and restores `ProductVariant.stockQuantity` for central variant returns; product-level `Inventory` is restored only for non-variant central items. Retailer-fulfilled returns continue to restock retailer inventory only.

## Verification

Static targeted checks:
- Historical fulfillment detection for central-stock transfer: PASS
- Rejection restricted to ASSIGNED: PASS
- Central variant return restores `ProductVariant.stockQuantity`: PASS
- Central non-variant return restores global `Inventory`: PASS
- `ReturnProcessingService` constructor receives `ProductVariantRepository`: PASS
- Edited Java brace balance: PASS

Environment/runtime boundary:
- Java 21 available.
- Maven wrapper is present, but dependency download could not run because this environment has no DNS/network access to `repo.maven.apache.org`.
- Therefore Maven compile/test, PostgreSQL/Redis integration, Docker, and browser E2E remain runtime-unverified.

## Migration integrity
No existing Flyway migration was modified and no new migration was required for these source-only lifecycle fixes.

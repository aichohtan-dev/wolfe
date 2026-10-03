# Wolfe — Fresh 100-Finding Recheck / Fix Batch 3

Date: 2026-10-01
Base: `Wolfe-V49-fresh-100-recheck-fix-batch2.zip`

## Five confirmed residual findings

### 1. Multiple-bundle discount invariant used the global allocation sum
`OrderService` validated each bundle's allocated discount against `lineDiscounts.values()` for *all* bundles. An order containing two independently discounted bundles could therefore fail the second bundle's invariant even when each bundle was individually correct.

**Fix:** validate only the current bundle's line allocations and preserve the global sum only for final subtotal calculation.

### 2. Central stock was released immediately after retailer assignment
After `allocateOrder()` succeeded, the order creation path restored variant stock and released global reservations before the retailer accepted the assignment. A later retailer rejection/expiry could leave the active order without central fallback reservation and permit oversell.

**Fix:** central reservation remains authoritative until retailer acceptance. Acceptance performs the one-time authority transfer and releases central stock.

### 3. Retailer cancellation could double-release central inventory
Because finding #2 released central stock at assignment time, `OrderService.transition(...CANCELLED)` later released the same central reservation again and restored variant stock a second time.

**Fix:** cancellation now releases central product/variant stock only while the order is still centrally reserved; retailer-assigned accepted orders release only retailer reservation.

### 4. Retailer delivery attempted to fulfill already-transferred global stock
`RetailerAllocationService.deliverOrder()` called global `Inventory.fulfill()` even though retailer acceptance had transferred inventory authority away from central stock. The central reservation had already been released, so this could fail with an invalid fulfillment quantity or corrupt the global stock model.

**Fix:** retailer delivery consumes only retailer inventory. Central delivery fulfillment remains in `OrderService` for orders with no retailer fulfillment.

### 5. Return refund state could be marked REFUNDED before completion
Admin return mutation allowed `refundStatus=REFUNDED` while the return remained PENDING/APPROVED/RECEIVED. That created an invalid financial lifecycle state.

**Fix:** `REFUNDED` now requires `COMPLETED` in the same transition; completion still validates the selected-item refund ceiling.

## Additional correction in same lifecycle area
Retailer-fulfilled returns no longer increment global physical inventory. Global restock occurs only for centrally fulfilled returns; retailer-fulfilled returns restock the retailer authority.

## Verification gates
- Multi-bundle invariant static check: PASS
- Central reservation release moved to retailer acceptance: PASS
- Cancellation central-release guard: PASS
- Retailer delivery no longer calls global `Inventory.fulfill`: PASS
- Retailer return no longer increments global quantity: PASS
- REFUNDED-before-COMPLETED guard: PASS
- Migration history preserved; no existing migration modified

## Runtime boundary
Maven/Docker/PostgreSQL/Redis/browser runtime certification remains UNVERIFIED in this environment. These changes are source-level verified and must pass the full runtime CI/E2E gate.

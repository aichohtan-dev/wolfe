# Wolfe — Fresh 100-Finding Recheck / Fix Batch 2

Date: 2026-10-01
Base: `wolfe-v49-fresh-100-findings-fix-batch1.zip`

## Result
A second source-first line-level recheck was performed against the Batch-1 source. Five residual issues were found and corrected:

1. **API error-message leakage** — `ApiExceptionHandler.safeClientMessage()` could still reflect identifiers such as `WLF-*` order IDs or short numeric IDs. Replaced the broad pass-through with a strict allow-list of intentionally user-facing validation messages.
2. **Partial-return refund truncation** — `ReturnProcessingService.maxRefundableAmount()` used integer division, which could under-allocate proportional refunds. Switched to `BigDecimal` + `HALF_UP` per-line proportional arithmetic.
3. **Retailer service-area query repetition** — `RetailerAllocationService.evaluateEligibleRetailers()` queried the same pincode service-area set once per retailer. The pincode-scoped rows are now fetched once and filtered in memory per retailer.
4. **Bare `orElseThrow()` in stock-release paths** — two remaining no-argument throws in `OrderService` were replaced with contextual `IllegalStateException` messages that do not expose internal IDs.
5. **Nginx privileged-port capability mismatch** — frontend Compose dropped all Linux capabilities while Nginx listens on 80/443. Added only `NET_BIND_SERVICE`, retaining `cap_drop: ALL` otherwise.

## Recheck gates
- Frontend contract tests: **8/8 PASS**
- Frontend unit harness: **1/1 PASS**
- Security scan: **269 files PASS**
- package.json parse: **PASS**
- Compose targeted security/resource checks: **PASS**
- Bare zero-argument `orElseThrow()` sweep: **0 remaining**
- Retailer pincode service-area query in evaluator: **1 query per evaluation**
- Partial refund arithmetic: **HALF_UP**
- Internal-ID error reflection: **blocked by strict allow-list**
- Nginx privileged-port capability: **NET_BIND_SERVICE only**

## Runtime boundary
Maven/Docker/PostgreSQL/Redis/browser runtime certification remains UNVERIFIED in this environment. These changes are source-level verified and must still pass the full runtime CI gate.

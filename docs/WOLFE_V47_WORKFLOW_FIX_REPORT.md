# Wolfe Workflow Fix Report

## Fixes
- Admin order status endpoint is now constrained by retailer fulfillment state: PROCESSING requires retailer acceptance; SHIPPED requires OUT_FOR_DELIVERY; DELIVERED requires retailer delivery.
- Admin UI no longer exposes retailer-owned PROCESSING/SHIPPED/DELIVERED transitions; cancellation remains available for non-terminal orders.
- Return eligibility is anchored to the recorded retailer `deliveredAt` timestamp, not order creation time.
- Refunds marked REFUNDED are capped by the selected return quantities and their server-calculated line-net value; full returns may include the order-level shipping amount.
- Partial return completion continues to restock only selected quantities and applies the same quantities to retailer inventory and settlement adjustment.

---
**Certification note:** Historical/archival report. It is not runtime certification evidence. See `docs/REPORTING_POLICY.md` and the current Zero-Gap Master Findings ledger for authoritative certification status.

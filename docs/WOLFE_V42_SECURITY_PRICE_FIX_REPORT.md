# Wolfe v42 Security + Pricing Fix Report

Fixed the v41 audit findings for active-account JWT enforcement, refresh revocation, subject+IP rate limiting, cancellation coupon usage, retailer rejection races, admin delivery gating, mandatory variant SKUs, exact order-line net amounts including coupon allocation, return locking/window, COD settlement reconciliation, settlement locking, and variant stock-authority confusion.

Money contract: catalog prices are rupee BigDecimal; checkout/order/coupon/shipping/settlement values are integer paise. Coupon discount is allocated to order lines and settlement uses exact line_net_amount rather than truncated unit-price multiplication.

---
**Certification note:** Historical/archival report. It is not runtime certification evidence. See `docs/REPORTING_POLICY.md` and the current Zero-Gap Master Findings ledger for authoritative certification status.

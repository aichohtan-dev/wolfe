# Wolfe V46 Security Hardening Report

## Confirmed fixes
- Retailer fulfillment A -> B -> A reassignment is now append-only. Historical fulfillment rows are never retargeted.
- Removed `(order_id, retailer_id)` fulfillment uniqueness and retained one active fulfillment per order via a partial unique index that excludes CANCELLED, FAILED_DELIVERY, REASSIGNED and RETURNED rows.
- Assignment and fulfillment retailer-pair lookups now select the latest row, preventing historical duplicate pairs from causing `Optional`/non-unique failures.
- Settlement retailer-pair lookup in the retailer portal is history-safe.
- Registration no longer exposes `EMAIL_EXISTS`; both outcomes return the same 202 response body and duplicate probes still perform BCrypt work.
- Authentication cookies are secure-by-default.
- HSTS is emitted at the Nginx edge.
- Node toolchain is pinned to 22.23.2 in development and CI.
- Apache PDFBox upgraded to 3.0.8, the current 3.0.x release.
- Added V39 fulfillment-history migration and an A->B->A regression test.

## External verification basis
OWASP recommends generic registration/authentication responses to prevent account enumeration. Apache PDFBox lists 3.0.8 as the fixed release for CVE-2026-33929.

## Verification limits
Java 21 and Node/npm are available in this container, but Maven and Docker CLIs are not installed. Full Maven test execution and live container/DB verification therefore remain CI/target-environment checks.

---
**Certification note:** Historical/archival report. It is not runtime certification evidence. See `docs/REPORTING_POLICY.md` and the current Zero-Gap Master Findings ledger for authoritative certification status.

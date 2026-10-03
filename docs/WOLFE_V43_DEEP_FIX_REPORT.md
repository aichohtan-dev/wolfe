# Wolfe v43 — Deep Security/Integrity Fix Report

## Basis
Applied after the Cloudflare `security-audit` methodology deep pass. Source-first findings were reviewed and the high-impact findings were remediated without editing historical Flyway migrations.

## Fixed in v43
- COD reconciliation expected amount is server-authoritative and cannot be replaced by a client-supplied expected amount.
- Settlement reassignment creates a new settlement after a previous retailer settlement is adjusted; latest settlement lookup is used for eligibility.
- Settled payouts are not mutated for returns; return adjustments are recorded in a separate immutable adjustment ledger.
- Retailer delivery uses a deterministic global-inventory-before-retailer-inventory lock order.
- Retailer rejection releases only the retailer reservation; the global order reservation remains held until order cancellation/fulfillment.
- Bundle composition validation now compares exact product quantities rather than only product sets.
- PDF import staging media is no longer served as public `/catalog/imports/**`; only approved media under `/catalog/published/**` is public.
- PDF `REVIEW_REQUIRED` items can be approved after required fields are explicitly corrected; rejected/approved items cannot be approved again.
- Approved PDF images are copied into the public published-media area and item/product URLs are updated.
- Anonymous configuration creation is rate-limited.
- Admin variant request now accepts and persists the frontend's full variant contract (title, color, material, size, finish, dimensions, stock, image and attributes).
- Added settlement adjustment ledger migration/entity/repository.

## Verification limitations
- Maven compilation could not execute because the Maven wrapper attempted to download Maven 3.9.11 and DNS/network access is unavailable in this environment.
- Frontend typecheck was attempted; the environment lacks installed React/React DOM type dependencies, so TypeScript emitted dependency/cascade errors. No new error referencing the v43 backend changes was observed in that output.
- Docker runtime verification was not available.

## Release status
Source-level hardening is applied. Runtime compile/test and Docker verification remain release gates.

---
**Certification note:** Historical/archival report. It is not runtime certification evidence. See `docs/REPORTING_POLICY.md` and the current Zero-Gap Master Findings ledger for authoritative certification status.

# Wolfe V30 Cross-Module Security & Integrity Fix Report

## Scope
This variant applies the first remediation batch from the deep cross-module audit. No unrelated feature work was added.

## Fixes applied
1. **Production secret fail-fast**
   - Removed the hardcoded JWT secret fallback.
   - Removed the hardcoded PostgreSQL password fallback.
   - Missing `WOLFE_JWT_SECRET` is now rejected by the existing `JwtService` constructor.

2. **Variant-aware persistent cart**
   - Added `variant_id` to persisted cart items.
   - Reworked cart primary key to a generated ID.
   - Added uniqueness for customer + product + exact variant (including base-product cart lines).
   - Cart API now validates that a variant belongs to the requested product.
   - Frontend account sync and cart mutations now persist variant IDs.

3. **Strict order variant validation**
   - Invalid/inactive variant SKU now fails the order instead of silently falling back to the base product.
   - Variant SKU is checked against the requested product.
   - If both variant ID and SKU are supplied, they must identify the same variant.

4. **Retailer/global inventory delivery consistency**
   - Retailer delivery now finalizes the Wolfe/global inventory reservation in the same transaction as retailer stock fulfillment.
   - This prevents the previous global reservation leak when delivery was completed through the retailer workflow.

5. **Retailer allocation failure visibility**
   - Removed swallowed allocation exceptions during order creation and cancellation.
   - Order creation now rolls back if retailer allocation is required but cannot be completed.

6. **PDF import anti-fabrication hardening**
   - Removed invented default price/brand/material/color/finish/size/dimensions values from extraction fallbacks.
   - Missing values remain unresolved and lower confidence.
   - Review-required items cannot be directly approved.
   - `Approve All` no longer promotes `REVIEW_REQUIRED` items.
   - Approval requires verified name, price, brand, category, finish, material, and color.

## Database migration
Added:
- `backend/src/main/resources/db/migration/V30__cart_variants_and_security_hardening.sql`

The migration is additive and preserves existing cart rows by assigning them generated IDs and leaving their variant ID null.

## Verification performed
- `npm run typecheck` — **PASS**
- `git diff --check` — **PASS**
- Backend Maven tests — **NOT EXECUTED** in this environment because the Maven wrapper attempted to download Maven from Maven Central and external DNS/network access was unavailable.
- Browser/runtime verification — **NOT claimed** for this ZIP variant.

## Important remaining findings
This variant does **not** yet resolve every finding from the deep audit. Remaining work includes PDF resource limits/async processing, retailer radius/hours/capacity rules, public configuration-token privacy review, API pagination caps, HttpOnly refresh-token architecture, retailer rejection reallocation, and broader inventory-authority cleanup.

## Integrity handoff
This ZIP is intended to be extracted and independently reviewed in the user's GitHub/Antigravity environment. Do not treat the local ZIP as a replacement for the Git repository until the extracted source passes the project's normal CI, migration, backend test, frontend build, and browser/E2E verification.

---
**Certification note:** Historical/archival report. It is not runtime certification evidence. See `docs/REPORTING_POLICY.md` and the current Zero-Gap Master Findings ledger for authoritative certification status.

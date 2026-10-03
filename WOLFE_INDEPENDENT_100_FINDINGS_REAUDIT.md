# Wolfe — Independent 100-Finding Re-Audit

Date: 2026-10-01
Baseline: V49 / Batch 12

## Scope

The complete authoritative `WOLFE_ZERO_GAP_FINDINGS_001_100_MASTER.md` ledger was re-opened and checked against the current source/configuration rather than accepting the previous Batch-12 summary as evidence.

## Re-audit result

- Ledger rows: **100/100 present**
- Green: **100**
- Yellow: **0**
- Red: **0**
- Bare zero-argument `orElseThrow()`: **0**
- Duplicate Flyway versions: **0**
- Latest Flyway version: **V56**
- Java entity ID/version invariant sweep: **PASS**
- Frontend contract tests: **8/8 PASS**
- Lightweight security scan: **PASS (268 files)**
- Shell syntax checks: **PASS**
- GitHub Action tag-pinning sweep: **PASS**
- Static CI JWT test-secret sweep: **PASS**
- Public/customer Product response DTO sweep: **PASS**

## Additional regressions found during this independent pass

Four implementation regressions/hardening issues were discovered that had escaped the earlier Batch-12 static summary. All four were corrected before this checkpoint was produced:

1. **Product/Retailer JPA ID mapping regression**
   - `Product.java` and `Retailer.java` had accidentally placed `@Id`/`@GeneratedValue` on the `version` field while their getters referenced a missing `id` field.
   - Corrected to separate generated `Long id` and `@Version long version` fields, matching the database schema and optimistic-concurrency migrations.

2. **Product DTO boundary regression**
   - `ProductController.list()` declared `List<Product>` while returning `ProductPublicView` DTOs.
   - Public related-product, wishlist, recent-product, and PDF-approval responses could still expose the entity type.
   - Corrected these endpoints to return `ProductPublicView` DTOs; the DTO constructor was made public for cross-package use.

3. **SEO absolute social-image regression**
   - The prerenderer made canonical/OG URLs absolute but left the base `og:image` relative.
   - Corrected prerendering so generated `og:image` is `${VITE_PUBLIC_BASE_URL}/wolfe-logo.png`.

4. **CI secret hygiene improvement**
   - The backend CI test job contained a static JWT test secret.
   - Replaced it with an ephemeral `openssl rand -hex 32` secret exported through `GITHUB_ENV`.

## Important evidence boundary

This re-audit is a **static/contract re-audit**. It does not claim runtime certification. Full Maven/Spring Boot/PostgreSQL/Redis/Docker/browser execution remains dependent on a network-enabled/cached CI environment with Docker available.

The CI workflow contains the runtime gates; their actual execution result must still be captured before final release certification.

## Whole-project source-code re-audit addendum

The implementation was subsequently audited across the entire current project tree rather than only against the 100-row ledger. Additional source-level gaps were found and corrected, including PDF upload DTO leakage, public visual/accessory entity exposure, review identity exposure, password-reset refresh-session revocation, recovery-email rate limiting, session-cap concurrency, customer return admin-note exposure, and cart-recovery token exposure. See `WOLFE_SOURCE_CODE_FULL_PROJECT_REAUDIT.md` for the evidence and verification boundary.

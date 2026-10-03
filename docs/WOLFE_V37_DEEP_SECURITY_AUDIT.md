# Wolfe V37 — Deep Security Audit & Hardening

## Scope
Base: `wolfe-v36-experience-contract-fixed.zip`

Audit covered:
- Spring Security / JWT / refresh-token rotation
- customer IDOR/ownership boundaries
- retailer multi-tenant isolation
- public/private experience APIs
- admin authorization surfaces
- sensitive response serialization
- input validation / resource-abuse controls
- PDF import attack surface
- CORS / CSP / security headers
- secret/configuration exposure
- dangerous code/sink scan
- Flyway migration integrity
- frontend token storage

## Confirmed fixes applied

### S1 — Refresh-token rotation race
**Severity: High**

Previous flow performed `find -> active check -> revoke -> save` without a database row lock/transaction. Concurrent refresh requests could potentially consume the same refresh token before either transaction committed.

Fix:
- `SessionService.rotate()` is now `@Transactional`.
- `RefreshTokenRepository.findByTokenHashForUpdate()` uses `PESSIMISTIC_WRITE`.
- blank refresh tokens are rejected before hashing.

No migration required.

### S2 — Public experience exposed inactive catalog content
**Severity: Medium**

Public experience endpoints could resolve a product without requiring `active=true`, and a visual asset without requiring the asset itself to be active.

Fix:
- public spin/visual-asset product resolution requires active product.
- visual asset response requires active asset.

### S3 — Public hotspot exposure was not tied to active visual content
**Severity: Medium**

Active hotspots could be returned for an inactive visual-content record when the public endpoint was queried directly by ID.

Fix:
- public hotspot query now requires both hotspot active and parent visual content active.

### S4 — Shared configuration response leaked customer ownership metadata
**Severity: Medium**

The public share endpoint returned the persistence entity, including `customerId`.

Fix:
- introduced `SharedConfigurationResponse` without `customerId`.
- share token format is validated before database lookup.

### S5 — Quote request could attach another customer's configuration
**Severity: Medium**

A customer could submit an arbitrary `configurationId` unrelated to their own configuration.

Fix:
- customer-owned configurations must belong to the authenticated customer.
- product/configuration relationship is validated when both IDs are supplied.
- intentionally public/anonymous configurations remain shareable.

### S6 — Suspended/inactive retailers could still reach retailer portal operations
**Severity: High**

Role-based routing allowed `ROLE_RETAILER`, but the retailer lookup did not enforce the retailer's active/verified state.

Fix:
- linked retailer must pass `Retailer.isActive()` before portal access is granted.
- applies to both `userId` and email fallback resolution.

### S7 — Retailer inventory input hardening
**Severity: Medium**

Inventory adjustment and fulfillment metadata lacked complete request-size/non-negative validation.

Fix:
- stock quantity requires `@Min(0)`.
- SKU/reason/tracking/courier lengths bounded.
- retailer pack/out-for-delivery request bodies now use `@Valid`.

### S8 — API error boundary hardening
**Severity: Medium**

Controller-level `AccessDeniedException` could otherwise escape as an unstructured server error, and unexpected exceptions had no controlled JSON boundary.

Fix:
- explicit 403 response for access denial.
- explicit 400 response for bean-validation failures.
- generic unexpected exceptions return a controlled 500 response without internal exception details.

## Additional hardening

- Public brand-by-slug endpoint now hides inactive brands.
- Customer registration/login/profile inputs received explicit length limits.
- Nginx now disables `server_tokens`.
- Nginx adds HSTS and `X-Permitted-Cross-Domain-Policies: none`.

## Static audit results

- Dangerous Java/JS sink scan: no `Runtime.exec`, `ProcessBuilder`, Java object deserialization, `eval`, `new Function`, `dangerouslySetInnerHTML`, or direct `innerHTML` found in application source.
- Hardcoded-secret pattern scan: no matching hardcoded credential found in application source.
- Flyway migrations: 30 files, versions 1–31 with 30 unique versions; no duplicate version detected.
- Existing `package-lock.json` checksum unchanged from V36.
- No Flyway migration was added or modified by V37.

## Remaining security items — not silently changed

### R1 — Browser token storage
Access and refresh tokens remain in `localStorage`. A future XSS compromise could expose them. Stronger architecture is HttpOnly/Secure/SameSite cookie-based refresh handling with short-lived in-memory access tokens.

### R2 — Runtime security tests
The repository has retailer-focused security tests, but does not contain a comprehensive MockMvc/API security matrix covering every customer/admin/retailer endpoint.

### R3 — PDF import resource exhaustion
The importer has file/page/image limits, but PDFBox processing still loads the uploaded byte array into memory and can perform expensive PDF/image decoding. Production should isolate large PDF processing asynchronously with strict CPU/memory/time quotas.

### R4 — Admin URL/media trust boundary
Admin-controlled media/link URLs are rendered by the public experience. The admin is already a privileged trust boundary, but production hardening should restrict URL schemes to `https:` and approved relative paths where applicable.

### R5 — HSTS deployment prerequisite
HSTS is safe/effective only when the production domain is served over HTTPS and all covered subdomains are HTTPS-capable. Verify this before production rollout.

## Verification limitation

Full Maven and frontend typecheck/build could not be certified in this environment:
- Maven wrapper attempted to download Maven 3.9.11 but DNS/network access to `repo.maven.apache.org` failed.
- `node_modules` was absent in the source ZIP; an attempted offline-preferred npm install timed out and did not provide the complete dependency/type set.
- `npm run typecheck` therefore stopped on missing React/Babel/PropTypes type definitions.

Therefore this report is **source/static security hardening verified**, not a claim of full runtime/build certification.

## Change control

- Base: V36
- New version: V37
- No commit
- No push
- No existing Flyway migration modified

---
**Certification note:** Historical/archival report. It is not runtime certification evidence. See `docs/REPORTING_POLICY.md` and the current Zero-Gap Master Findings ledger for authoritative certification status.

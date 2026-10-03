# Wolfe V38 — IDOR / Privilege-Escalation Deep Security Audit

## Scope
Base artifact: Wolfe V37 Deep Security Audit & Hardening.

Audit focus:
- Customer-to-customer IDOR boundaries
- Retailer cross-tenant isolation
- Admin / Super Admin privilege boundaries
- JWT role trust and stale-role access
- Method-level authorization enforcement
- Public/private experience data boundaries
- Sensitive resource ownership checks
- Security matcher vs controller authorization alignment

## Confirmed findings and fixes

### S1 — JWT role claim was trusted instead of current account role
**Severity: High**

The JWT contained a `role` claim and `JwtAuthFilter` used that claim directly to construct Spring authorities. If a customer's role changed or was revoked in the database, an already-issued access token could continue carrying the previous authority until token expiry.

Fix:
- `JwtAuthFilter` now resolves the customer from the signed `customerId` claim.
- Current `Customer.role` is loaded from PostgreSQL for every authenticated request.
- Missing/deleted customer or invalid role now produces `401`.
- Newly issued JWTs no longer include the redundant `role` claim; the existing method signature is retained for caller compatibility.

Result: privilege changes in the database are reflected immediately for bearer-token authorization.

### S2 — Method-level security annotations were not enabled
**Severity: High (defense-in-depth gap)**

Several controllers used `@PreAuthorize`, but the application did not enable Spring method security. URL-level `/api/v1/admin/**` protection reduced immediate exposure, but the annotations themselves were inert and would not protect a future route/matcher mistake.

Fix:
- Added `@EnableMethodSecurity` to `WolfeApplication`.
- Updated existing admin annotations from `hasRole('ADMIN')` to `hasAnyRole('ADMIN','SUPER_ADMIN')` so enabling method security does not unintentionally block Super Admin.

### S3 — Admin/Super Admin could enter retailer portal authorization path
**Severity: High**

SecurityConfig allowed `ROLE_ADMIN` and `ROLE_SUPER_ADMIN` on `/api/v1/retailer/**`. `RetailerController` then resolved a retailer by user ID or email without requiring the authenticated principal itself to have `ROLE_RETAILER`.

This created an unnecessary cross-role privilege path and could expose retailer operational data/actions to an admin principal whose account matched a retailer record.

Fix:
- Retailer portal now requires `ROLE_RETAILER` in `getAuthenticatedRetailer()`.
- Retailer must also pass `Retailer.isActive()` (ACTIVE + VERIFIED).
- Admin continues to use `/api/v1/admin/retailers/**` for retailer administration.

### S4 — Recent-viewed customer data could return inactive products
**Severity: Low/Medium**

Customer-owned recent records were correctly isolated by customer ID, but the product lookup did not require the product to remain active.

Fix:
- Recent-product response now filters through `Product::isActive`.

## Authorization matrix — source-level verification

| Surface | Anonymous | Customer | Retailer | Admin | Super Admin |
|---|---|---|---|---|---|
| Public catalog/products | Allow | Allow | Allow | Allow | Allow |
| Customer cart/wishlist/address/order | Deny | Own customer only | Deny | Deny via customer ownership | Deny via customer ownership |
| Customer reviews POST | Deny | Authenticated + purchase check | Authenticated + purchase check | Authenticated + purchase check | Authenticated + purchase check |
| Retailer portal | Deny | Deny | Own retailer only | Deny | Deny |
| Admin catalog/operations | Deny | Deny | Deny | Allow | Allow |
| Admin retailer management | Deny | Deny | Deny | Allow | Allow |
| Public consultation POST | Allow | Allow | Allow | Allow | Allow |
| Consultation admin endpoints | Deny | Deny | Deny | Allow | Allow |

The matrix is derived from `SecurityConfig` plus controller/service ownership checks. It is not a runtime HTTP certification.

## Customer IDOR checks

Verified source-level ownership guards for:
- addresses
- cart
- wishlist
- notifications
- orders and order history/cancel
- quotes
- custom-design requests
- recent products
- back-in-stock subscriptions
- cart-recovery touch
- customer-specific returns

The common `CustomerAccess.requireCustomer()` check compares the path/body customer ID with the authenticated principal's customer ID.

## Retailer cross-tenant checks

Verified:
- retailer inventory queries are scoped by authenticated retailer ID
- inventory movements are scoped by retailer ID
- settlements are scoped by retailer ID
- assigned-order lookup uses retailer ID
- fulfillment state changes verify fulfillment retailer ID
- order acceptance/rejection uses order + retailer assignment
- retailer portal now requires `ROLE_RETAILER` and active/verified retailer state

## Remaining security work

1. Runtime MockMvc/API matrix is still required to prove 401/403 behavior across every endpoint.
2. Browser token storage remains `localStorage`; HttpOnly/Secure/SameSite refresh-cookie architecture would reduce token theft impact from XSS.
3. PDF import remains a resource-exhaustion surface requiring production CPU/memory/time isolation for untrusted large PDFs.
4. Admin-controlled media URLs should be constrained to HTTPS/approved relative paths.
5. HSTS rollout requires the production domain and covered subdomains to be HTTPS-ready.

## Verification

Static/source checks performed:
- `@EnableMethodSecurity` present.
- All existing `@PreAuthorize` expressions aligned for ADMIN + SUPER_ADMIN.
- No remaining application use of JWT `role` claim for authorization.
- Retailer portal has explicit `ROLE_RETAILER` check.
- Retailer active/verified check is enforced by `Retailer.isActive()`.
- Customer ownership guards remain present on customer-specific endpoints.
- Public recent-product response filters inactive products.
- ZIP integrity check passed.

Full Maven compile and frontend typecheck/build were not certified because the V37 source artifact does not include installed dependencies and the environment previously lacked Maven Central DNS/network access. No runtime-green claim is made.

## Change control

- Base: V37
- New version: V38
- No Git commit
- No Git push
- No Flyway migration added or modified

---
**Certification note:** Historical/archival report. It is not runtime certification evidence. See `docs/REPORTING_POLICY.md` and the current Zero-Gap Master Findings ledger for authoritative certification status.

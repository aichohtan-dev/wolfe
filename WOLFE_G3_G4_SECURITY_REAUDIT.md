# Wolfe G3/G4 Security Re-Audit — V49 Deep Security Checkpoint

## Scope
Current checkpoint re-audited from source after the V49 deep-security fixes.

## G3 — Authentication / Session / Authorization

| Skill | Status | Evidence |
|---|---|---|
| JWT signature/expiry | PASS | `JwtService.parse()` verifies signed claims; access TTL is configurable. |
| JWT privilege source | PASS | `JwtAuthFilter` resolves current role from the customer row; JWT does not carry an authoritative role claim. |
| Session-version revocation | PASS | Filter rejects tokens whose `sessionVersion` differs from the current customer row. |
| Refresh rotation | PASS | Refresh token is row-locked, revoked before replacement, and reuse revokes the customer's active token family. |
| Refresh reuse transaction | PASS | `rotate()` uses `noRollbackFor = InvalidRefreshTokenReuseException.class`, so reuse containment commits. |
| Login timing | PASS | Unknown/disabled/locked accounts execute a dummy BCrypt comparison. |
| Login rate limiting | PASS | Hard limits are IP and subject+IP; subject-only counter is telemetry/soft and cannot deny a victim account. |
| Customer ownership | PASS | `CustomerAccess.requireCustomer()` is applied to customer-scoped cart, wishlist, order, quote, custom-design and experience endpoints reviewed. |
| CSRF | PASS | Spring CSRF is enabled; frontend obtains `XSRF-TOKEN` and sends `X-XSRF-TOKEN` on state-changing requests. |
| Stale optional access cookie | PASS | Invalid access cookie clears authentication and lets Spring authorization decide public vs protected access. |
| Logout/refresh cookies | PASS | HttpOnly, Secure-configurable, Strict SameSite; refresh cookie is path-scoped. |
| Role boundary | PASS | Retailer/admin/consultation/actuator authorization rules align with current authorities. |

## G4 — Catalog / Cart / Configurator / Bundle

| Skill | Status | Evidence |
|---|---|---|
| Catalog filtering/paging | PASS | Product pages are bounded to 100 items; sort is allow-listed. |
| Variant ownership | PASS | Cart and order paths verify variant belongs to selected product and is active. |
| Bundle membership | PASS | Cart/order verify selected product belongs to active bundle. |
| Bundle unit integrity | PASS | Order creation requires all configured bundle products and whole bundle units. |
| Bundle discount validation | FIXED | Admin bundle writes now require `FIXED`/`PERCENT`; percent discounts are capped at 100 and values are non-negative; DB migration also enforces allowed type and non-negative value. |
| Configuration ownership | PASS | Customer-owned configurations cannot be attached to another customer's cart/order. |
| Configuration token entropy/expiry | PASS | 128-bit UUID-derived token; public share expires after 30 days and validates token format. |
| Configuration price authority | PASS | Checkout recomputes current product/accessory prices server-side. |
| Accessory/product relationship | PASS | Accessory must be active and belong to the selected product. |
| Rate-limit IP trust boundary | FIXED | Customer, experience, order quote, quote, custom-design and consultation paths now use server-observed `remoteAddr`; no application rate-limit caller trusts client forwarding headers. |
| Cart customer boundary | PASS | All customer cart mutations/read/delete operations require matching authenticated customer ID. |
| Wishlist customer boundary | PASS | All customer wishlist operations require matching authenticated customer ID. |

## Verification limitation
Maven execution remains **NOT VERIFIED** because this environment cannot resolve `repo.maven.apache.org`. No runtime/build PASS is claimed.

## Gate disposition
- G3 static/security re-audit: **PASS**
- G4 static/security + contract re-audit: **PASS** after the two fixes above
- Runtime integration verification: **NOT VERIFIED**

---
**Certification note:** Historical/archival report; not runtime certification evidence. See `docs/REPORTING_POLICY.md` and the current Zero-Gap Master Findings ledger for authoritative status.

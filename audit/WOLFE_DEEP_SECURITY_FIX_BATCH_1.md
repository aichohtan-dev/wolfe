# Wolfe Deep Security Fix Batch 1

## Scope
Confirmed high/medium findings from the post-G2 deep static audit were patched against the current V49/G2 source snapshot.

## Fixed
1. Invalid/expired access cookie no longer causes a blanket 401 in `JwtAuthFilter`; public `permitAll` routes continue anonymously and protected routes are rejected by Spring Security's authentication entry point.
2. Retailer identity lookup now trusts only `Retailer.userId`; customer email fallback removed.
3. Duplicate identical checkout lines are merged before discount allocation; merged quantity remains bounded.
4. Bundle membership no longer requires definition quantity == 1; expected bundle quantities are aggregated with `Integer::sum` and whole-unit validation is applied.
5. Configuration share payload is schema allowlisted; unsupported JSON fields/types are rejected. Share tokens use the full UUID and public shares expire after 30 days.
6. Access-cookie max-age is bound to `WOLFE_JWT_TTL_SECONDS`.
7. Refresh-token reuse revokes all active sessions for the affected customer.
8. Logout increments customer `session_version`; access JWTs carry the version and are rejected after logout/revocation.
9. Review purchase eligibility uses a single repository existence query instead of loading all customer orders/items.
10. Quote and custom-design creation endpoints now use Redis-backed rate limiting.
11. Retailer endpoints are restricted to `ROLE_RETAILER`; admin consultation routes are explicitly protected by SecurityConfig.
12. Phone/pincode validation was tightened for customer/order/address/retailer inputs.
13. CSP `connect-src` is now same-origin; hardcoded localhost origins removed.
14. `/actuator/info` is restricted to ADMIN/SUPER_ADMIN.
15. Spring API HSTS was removed because TLS terminates upstream; this keeps the HTTP-only container boundary consistent with G1 deployment architecture.
16. CSRF remains enabled via `CookieCsrfTokenRepository`; no `csrf.disable()` regression is present.

## Verification
Static assertions confirmed removal/presence of the targeted security patterns.

Runtime Maven compile/tests could not execute because this environment cannot resolve `repo.maven.apache.org`; therefore runtime certification remains pending for G11.

---
**Certification note:** Historical/archival report; not runtime certification evidence. See `docs/REPORTING_POLICY.md` and the current Zero-Gap Master Findings ledger for authoritative status.

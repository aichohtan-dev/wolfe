# Wolfe G1 — Architecture & Trust-Boundary Audit

**Date:** 2026-11-24  
**Repository:** `aichohtan-dev/wolfe`  
**Source baseline:** `main` @ `86208f6edf5bbb7104dc26bdf806c01970818692`  
**Audit branch:** `audit/zero-gap-full-2026-11-24`  
**Method:** Read-only source/configuration inspection. No application source changes and no runtime deployment were performed.

## G1 disposition

**PARTIAL — do not certify G1 PASS yet.**

The documented high-level architecture is broadly consistent with the source: React/Vite, Nginx, Spring Boot/Spring Security, PostgreSQL/Flyway and Redis-backed rate limiting. Several important security controls are present in code. However, the current Docker topology lacks network segmentation, and the edge TLS / trusted-proxy assumptions need a single verified deployment contract. A retailer onboarding trust-state issue was also identified for carry-forward to G5/G8.

## Architecture observed

Browser → Nginx → Spring Boot API → service/transaction layer → JPA repositories → PostgreSQL.

- Frontend: React 19, TypeScript, Vite, React Router; backend requests are centralized in `src/api.ts`.
- API: Spring Boot 3.5 / Java 21, Spring Security, stateless security filter chain, method security enabled.
- Data: PostgreSQL is authoritative; Flyway migrations are enabled, Hibernate uses `ddl-auto: validate`, `open-in-view: false`, Flyway clean is disabled.
- Redis: used by `RateLimitService` for distributed sliding-window authentication/request limits.
- Edge: Nginx serves the SPA, reverse-proxies `/api/`, and exposes ports 80/443 in Compose.
- Operations: Docker images are digest-pinned in Dockerfiles/Compose variables; API container is configured non-root, read-only, drops Linux capabilities, and does not publish port 8080 to the host in the supplied Compose file.

## Positive controls verified in source

1. **JWT validation and live revocation:** `JwtService` validates signature, issuer, audience and key ID. `JwtAuthFilter` resolves the customer from PostgreSQL on each authenticated request, checks `sessionVersion`, enabled/locked state, and derives authorities from the current database role.
2. **Refresh-token storage:** refresh tokens are random values; only SHA-256 hashes are persisted in the `refresh_tokens` table.
3. **Customer ownership:** `CustomerAccess.requireCustomer` compares the requested customer ID to the authenticated principal's server-set customer ID.
4. **Retailer isolation:** `RetailerController` requires ROLE_RETAILER, resolves retailer identity through the authenticated customer's `userId`, and rejects inactive/unverified retailer records. The earlier email fallback is absent in the inspected source.
5. **Admin authorization:** `/api/v1/admin/**` requires ADMIN/SUPER_ADMIN at URL level; individual AdminController operations apply permission authorities; AdminRetailerController has a class-level `PERM_ADMIN_RETAILER` requirement.
6. **CSRF/CORS:** CSRF remains enabled using CookieCsrfTokenRepository; frontend code obtains the XSRF token and sends `X-XSRF-TOKEN` for state-changing requests. CORS uses configured exact origins, credentials, and an explicit method/header allowlist.
7. **Stale cookie handling:** invalid optional access-cookie parsing clears authentication and continues the chain; protected-route authorization is then enforced by Spring Security.
8. **Container exposure:** PostgreSQL and Redis are `expose`-only (not host-published); API is `expose: 8080` only. Backend has `USER wolfe`, `read_only: true`, `cap_drop: ALL`, and `no-new-privileges`.

## New / carried-forward findings

### G1-TRUST-001 — Flat Docker network permits unnecessary lateral reachability

- **Severity:** P2
- **Status:** OPEN — source-confirmed configuration weakness
- **Evidence:** `docker-compose.yml` declares four services but no explicit `networks:` sections. Compose therefore attaches frontend, API, PostgreSQL and Redis to the same project default network. The public-facing frontend/Nginx container consequently has network reachability to the PostgreSQL and Redis service ports, even though it does not need those data-plane connections.
- **Impact:** If the public-facing frontend container is compromised, the attacker gains a direct network path to internal data services. Authentication on PostgreSQL/Redis remains a barrier, but the network trust boundary is broader than necessary.
- **Recommended remediation:** Create an `edge` network for frontend↔API and a separate `data` network for API↔PostgreSQL/Redis. Attach API to both; attach frontend only to edge; attach PostgreSQL/Redis only to data. Keep DB/Redis ports unpublished.
- **Closure proof:** Compose config inspection plus a runtime connectivity test showing frontend cannot connect to PostgreSQL/Redis while API can.

### G1-DEPLOY-002 — TLS termination topology is ambiguous and may cause redirect loops

- **Severity:** P2
- **Status:** NOT VERIFIED — deployment-dependent
- **Evidence:** `nginx.conf.template` unconditionally redirects port 80 to HTTPS and listens on 443 using mounted certificate files. The README describes TLS termination upstream at a cloud load balancer/edge proxy; the architecture document also allows external TLS termination.
- **Risk scenario:** If an upstream load balancer terminates client TLS and forwards plain HTTP to Nginx port 80, Nginx redirects back to HTTPS. The configuration does not contain an explicit trusted-upstream-`X-Forwarded-Proto` mode for this topology. This can create a redirect loop or mismatch the scheme seen by the application.
- **Recommended remediation:** Select and document one supported model: (A) TLS passthrough or re-encryption to Nginx 443 with origin certificates; or (B) trusted-LB termination with a dedicated origin-HTTP listener that honors forwarded scheme only from trusted proxy addresses. Do not blindly trust arbitrary client-supplied forwarded headers.
- **Closure proof:** A production-like test of HTTP→HTTPS redirect, HTTPS page load, Secure cookies, refresh, CSRF and correct scheme detection behind the actual edge proxy.

### G1-TRUST-003 — Client-IP trust boundary depends on proxy topology

- **Severity:** P2
- **Status:** NOT VERIFIED — linked to historical RATE-IP-01
- **Evidence:** Nginx rate-limit zones key on `$remote_addr`/`$binary_remote_addr`. `ops/proxy_params` overwrites `X-Real-IP` and `X-Forwarded-For` with Nginx's `$remote_addr`. Backend `server.forward-headers-strategy: framework` is enabled and auth rate limiting reads `request.getRemoteAddr()`. This is reasonable when Nginx directly receives client traffic. If a load balancer is in front, Nginx's remote address may be the load balancer address, causing many users to share a rate-limit bucket and security-log IP.
- **Recommended remediation:** If an upstream proxy is used, configure Nginx real-IP handling only for explicit trusted proxy CIDRs and test spoofed forwarding headers. Otherwise document Nginx as the direct public edge and keep upstream forwarding headers overwritten.
- **Closure proof:** Test direct-edge and deployed-proxy topologies, including a forged client `X-Forwarded-For` header, and verify distinct client buckets only when the trusted proxy chain supplies the address.

### G5-RETAILER-ONBOARD-001 — New retailer records default to trusted/active state

- **Severity:** P2
- **Status:** OPEN — source-confirmed business trust-state issue; carry to G5/G8
- **Evidence:** `Retailer.java` defaults `status=ACTIVE`, `verificationStatus=VERIFIED`, and `agreementStatus=SIGNED`. `AdminRetailerController.CreateRetailerRequest` does not require an explicit onboarding/verification state and `createRetailer` constructs the entity without overriding those defaults. `RetailerAllocationService.evaluateEligibleRetailers` selects all rows with ACTIVE + VERIFIED and does not require a linked `userId` or signed-agreement evidence before considering a retailer for allocation.
- **Impact:** A newly created partner can become allocation-eligible by default before the intended verification/agreement workflow is evidenced; a retailer with no linked login can also be selected for an order.
- **Recommended remediation:** Default new retailer records to PENDING/UNVERIFIED and agreement PENDING. Require explicit privileged activation after verification and agreement completion. Decide whether allocation must additionally require a linked active retailer account.
- **Closure proof:** Integration test proving a newly created retailer is not allocation-eligible until all required onboarding predicates are satisfied.

## Cross-gate evidence notes

- `G3RoleMatrixHttpSecurityTest` exercises URL matchers with Spring Security's synthetic `.with(user(...))` principal. It is useful for URL-level checks but does not prove the real JWT filter, cookie lifecycle, database role mapping, or all method-level permission boundaries. Keep WG20-P2-002 open until the real-token HTTP matrix is tested.
- `AdminRetailerController` returns JPA entities such as `Retailer`, `RetailerInventory`, and settlement records in multiple responses. This is already represented by WG20-P2-004 / ADMIN-DTO-01; do not duplicate it as a new G1 issue. Complete the DTO/serialization review in G8.
- `README.md` and `ENVIRONMENT.md` retain older V18.x-era instructions while `docs/ARCHITECTURE.md` describes V49. Treat this as documentation drift; update operator-facing docs after the architecture/deployment contract is finalized.
- Source inspection does not prove that production proxy CIDRs, certificates, secrets, firewall rules, Docker runtime, or browser behavior match these files.

## Files inspected

- `docs/ARCHITECTURE.md`, `README.md`, `ENVIRONMENT.md`, `WOLFE_G1_SKILL_ARCHITECTURE_CONSISTENCY.md`
- `backend/src/main/java/com/wolfe/WolfeApplication.java`
- `backend/src/main/java/com/wolfe/security/SecurityConfig.java`, `JwtAuthFilter.java`, `JwtService.java`, `SessionService.java`, `RolePermissions.java`, `CustomerAccess.java`, `RateLimitService.java`
- `backend/src/main/java/com/wolfe/customer/Customer.java`, `CustomerController.java`, `AccountLifecycleService.java`
- `backend/src/main/java/com/wolfe/admin/AdminController.java`, `AdminAuditInterceptor.java`, `AdminAuditLog.java`
- `backend/src/main/java/com/wolfe/retailer/Retailer.java`, `RetailerController.java`, `AdminRetailerController.java`, `RetailerRepository.java`, `RetailerAllocationService.java`
- `backend/src/main/java/com/wolfe/experience/ExperienceController.java`
- `backend/src/main/resources/application.yml`, `backend/pom.xml`, `package.json`, `src/api.ts`, `src/App.tsx`, `src/utils/storage.ts`
- `docker-compose.yml`, `Dockerfile.backend`, `Dockerfile.frontend`, `nginx.conf.template`, `ops/proxy_params`, `ops/nginx-entrypoint.sh`, `ops/redis-entrypoint.sh`
- `backend/src/main/resources/db/migration/V29__invisible_retailer_fulfillment_network.sql`, `V48__inventory_reconciliation_and_delivery_coordinates.sql`, `V56__document_historical_migration_gap.sql`
- `backend/src/test/java/com/wolfe/security/G3RoleMatrixHttpSecurityTest.java`, `JwtAuthFilterSecurityTest.java`, `backend/src/test/java/com/wolfe/retailer/RetailerSecurityTest.java`

## Next steps

1. Keep application code unchanged until G1 findings are reviewed and the intended production edge topology is known.
2. Add deterministic Compose network-isolation checks.
3. Verify the actual TLS/load-balancer topology and trusted client-IP chain.
4. Carry retailer onboarding trust-state finding into G5/G8 tests.
5. Proceed to G2 API contract inventory only after this G1 evidence checkpoint is recorded.

**G1 final status:** PARTIAL. No full architecture or production trust-boundary certification is claimed.

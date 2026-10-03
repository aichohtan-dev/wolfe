# Wolfe Runtime Environment Audit

**Date**: 2026-10-02  
**Auditor**: Antigravity Automated Environment Auditor  
**Scope**: Full workspace runtime inspection (Backend, Frontend, Databases, Proxy, Scripts, Containers, CI/CD, Configuration)

---

## A. PRESENT (Components Verified on Disk)

1. **Project Root & Structure**:
   - Monorepo containing React/Vite/TypeScript frontend at root (`src/`, `package.json`, `vite.config.ts`, `tsconfig.json`).
   - Spring Boot 3 / Java 21 backend in `backend/` (`backend/pom.xml`, `mvnw`, `mvnw.cmd`, `backend/src/`).
   - Operational tooling and scripts in `ops/` and `scripts/`.
   - CI workflows in `.github/workflows/` (`ci.yml`, `container-digest-policy.yml`, `docker-verify.yml`).

2. **Backend Engine**:
   - Maven Wrapper (`mvnw`, `mvnw.cmd`, `.mvn/wrapper/`).
   - Java 21 Temurin JDK runtime active on host (`openjdk 21.0.11`).
   - 159 compiled Java source classes across catalog, customer, order, inventory, retailer, security, review, and admin domains.
   - Spring Data JPA, Hibernate, HikariCP pool, Flyway, Spring Security, BCrypt, JJWT.
   - Actuator probes and Prometheus metrics configuration (`/actuator/health`, `/actuator/info`, `/actuator/metrics`, `/actuator/prometheus`).

3. **Database Schema & Migrations**:
   - 61 Flyway database migration scripts (`V1` through `V62`, with intentional historical omission of `V14`).
   - Tables for catalog, variants, orders, order items, retailer fulfillment network, settlements, margin rules, sessions, refresh tokens, audit logs, idempotency, PDF extraction jobs, and security events.

4. **Frontend Architecture**:
   - React 18.3.1, React Router DOM 6.30.1, Lucide React 0.511.0.
   - TailwindCSS 3.4.17 + PostCSS.
   - SEO prerendering engine (`scripts/prerender-seo.mjs`).
   - Frontend contract tests (`scripts/frontend-contract.test.mjs`).
   - Security scanner (`scripts/security-scan.mjs`).
   - Cookie-based authentication model with CSRF protection and localStorage consent gating (`src/utils/storage.ts`).

5. **Operational & Backup Infrastructure**:
   - `ops/backup-postgres.sh`, `ops/restore-postgres.sh`, `ops/verify-backup.sh`.
   - `ops/backup-media.sh`, `ops/restore-media.sh`, `ops/backup-all.sh`.
   - `ops/redis-entrypoint.sh`, `ops/nginx-entrypoint.sh`, `ops/proxy_params`.
   - `ops/preflight-v43-integrity.sh`.

6. **Container & Proxy Definitions**:
   - `Dockerfile.backend` (Multi-stage build, unprivileged user, healthcheck).
   - `Dockerfile.frontend` (Nginx reverse proxy, static asset serving, `/api/` upstream proxying).
   - `docker-compose.yml` (4 services: `postgres`, `redis`, `api`, `frontend`, resource limits, healthchecks).
   - `nginx.conf.template` (Security headers, CSP, gzip, rate limiting support).

---

## B. MISSING (Environment Components Not Yet Installed on Host)

1. **Host `node_modules` Directory**:
   - `node_modules/` is not currently installed on the host workspace (frontend dependencies need `npm ci --ignore-scripts` to run local `tsc` directly without global tools).
2. **Local Docker Engine on PATH**:
   - `docker` CLI is not present on host PATH. Dockerized orchestration runs in CI / containerized test runners.
3. **Local Redis CLI / Daemon on Host**:
   - `redis-cli` is not installed on host PATH (Redis runs in containerized test environments).
4. **Flyway Migration V14**:
   - Intentionally omitted historically (Flyway is configured to handle out-of-order / version gaps seamlessly).

---

## C. BROKEN (Identified & Remediated During Audit)

1. **CustomerController Method Signature**:
   - `CustomerController.java` line 117 had an accidental `@` prefix before `jakarta.servlet.http.HttpServletRequest request`. *(Remediated: Changed to `HttpServletRequest request`)*.
2. **CustomerController RateLimitService Argument Count**:
   - `CustomerController.java` line 142 called `rateLimits.check` with 2 arguments instead of 3. *(Remediated: Passed action, key, and clientIp)*.
3. **AdminController & AdminRetailerController Missing Imports**:
   - Missing `Authentication` and `jakarta.validation.constraints` imports in controllers. *(Remediated)*.
4. **ConfigurationRepository Missing Param Import**:
   - Missing `org.springframework.data.repository.query.Param` import for `@Param("cutoff")`. *(Remediated)*.
5. **PdfImportJob / PdfImportItem Missing Accessor Methods**:
   - Missing `setFilePath` on `PdfImportJob` and `setMediaUrls` on `PdfImportItem`. *(Remediated)*.
6. **QuoteRequestController Return Type**:
   - `mine()` method was returning `Page<QuoteRequest>` when method signature specified `List<QuoteRequest>`. *(Remediated: Appended `.getContent()`)*.
7. **RetailerAllocationConcurrencyContractTest Escaping**:
   - Unescaped inner quote in string assertion. *(Remediated)*.

---

## D. DUPLICATE / OBSOLETE

1. **Obsolete Root Patch Scripts**:
   - `patch.py` exists from historical migration passes. It is superseded by standard git tracked source files.
2. **Redundant Legacy Zip Archives**:
   - `f:\wolfe.zip` and `f:\wolfe1.zip` located in drive root contain outdated pre-audit snapshots (lacking V30–V62 migrations and Zero-Gap fixes). They are retained as immutable read-only backup references and must NOT be restored.

---

## E. REQUIRED FOR RUNTIME CERTIFICATION

1. **PostgreSQL 16 Instance**:
   - Accessible with user `wolfe`, database `wolfe`, executing migrations V1–V62.
2. **Redis 7 Instance**:
   - Accessible on port 6379 for distributed session tokens, refresh token rotation, and rate limiting counters.
3. **Clean Environment Secrets**:
   - Valid 64+ char `WOLFE_JWT_SECRET`.
   - Valid CORS origin `WOLFE_FRONTEND_ORIGINS=http://localhost:5173`.
   - `WOLFE_SECURE_COOKIES=false` for local HTTP-only dev test harnesses or `true` behind TLS reverse proxy.

---

## F. OPTIONAL

1. **OTLP Tracing Collector**:
   - Optional Jaeger/OTEL endpoint on `http://localhost:4318/v1/traces`.
2. **OpenAPI / Swagger UI**:
   - Disabled by default (`WOLFE_OPENAPI_ENABLED=false`) for security; enable only when inspecting contracts.
3. **Mock Mail Server**:
   - Mailhog / Inbucket for local verification token capture.

---

## G. SECURITY-SENSITIVE COMPONENTS

1. **JWT & Session Revocation**:
   - Single-use Refresh Token Rotation with automatic reuse detection (`SessionService.java`).
   - Customer session versioning (`Customer.incrementSessionVersion()`) immediately invalidates all active sessions upon password reset or admin lock.
2. **Multi-Tenant Retailer Isolation**:
   - Retailer endpoints enforce strict actor identity checks (`getAuthenticatedRetailer`).
   - Order fulfillment views hide customer PII and internal central stock allocations from cross-tenant inspection.
3. **BCrypt Timing Attack Defense**:
   - Constant-time password verification on login (`CustomerController.java`), performing BCrypt computation even for non-existent users.
4. **Strict HTTP Security Headers**:
   - CSP, X-Frame-Options DENY, X-Content-Type-Options nosniff, Referrer-Policy strict-origin-when-cross-origin.

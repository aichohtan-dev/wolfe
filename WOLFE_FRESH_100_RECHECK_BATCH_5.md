# Wolfe Fresh 100 Recheck — Batch 5

Base: `Wolfe-V49-fresh-100-recheck-fix-batch4.zip`

## Independent deep recheck finding

### High — Forwarded-client-IP spoofing could bypass backend rate limiting

**Evidence:**
- `backend/src/main/resources/application.yml` enables `server.forward-headers-strategy: framework`.
- Backend rate-limit code intentionally uses `HttpServletRequest#getRemoteAddr()` as the server-observed client address.
- Nginx previously forwarded `X-Forwarded-For` using `$proxy_add_x_forwarded_for`, which appends any client-supplied `X-Forwarded-For` value.
- With framework forwarded-header processing, an attacker-controlled forwarded address could therefore influence the backend's rate-limit bucket and related security-event remote address.

**Fix:**
- `ops/proxy_params` now overwrites `X-Forwarded-For` with Nginx's actual `$remote_addr` instead of appending the untrusted incoming header.
- `X-Real-IP` already used `$remote_addr` and remains unchanged.
- This keeps the existing `forward-headers-strategy: framework` design while making the proxy boundary authoritative for the client address.

## Regression checks

- `proxy_overwrite_xff` — PASS
- `forward_headers_framework_present` — PASS
- password-reset rate-limit path still uses server-observed `getRemoteAddr()` — PASS
- retailer central-stock history check from Batch 4 — PASS
- retailer rejection restricted to ASSIGNED — PASS
- retailer delivery does not fulfill central inventory — PASS
- central variant return restock remains present — PASS

## Runtime boundary

- Maven compile was attempted with the project wrapper but could not start because this environment cannot resolve `repo.maven.apache.org` to download Maven 3.9.11.
- Frontend dependency build was not run because `node_modules` is not present.
- PostgreSQL/Redis/Docker/browser E2E remain runtime-unverified.

## Migration integrity

No existing Flyway migration was modified. Batch 5 changes only the Nginx proxy header configuration and this audit report.

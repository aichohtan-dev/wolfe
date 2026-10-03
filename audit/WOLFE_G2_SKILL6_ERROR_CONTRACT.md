# Wolfe G2 Skill #6 — Error Contract / HTTP Status Mapping

## Result
PASS (static contract verification)

## Finding fixed
Security-layer 401/403 responses were not aligned with the canonical API error shape used by `ApiExceptionHandler` (`error` + `message`).

### Before
- JWT filter returned only `{"error":"invalid_or_expired_token"}`.
- Spring Security authentication/CSRF denial could return framework-default responses.

### After
- JWT filter returns HTTP 401 with `error=UNAUTHORIZED` and a safe `message`.
- Security `AuthenticationEntryPoint` returns HTTP 401 with the same canonical shape.
- Security `AccessDeniedHandler` returns HTTP 403 with `error=FORBIDDEN` and safe `message`.
- Existing API handler contracts remain: 400 validation, 404 not found, 409 conflict, 413 payload too large, 429 rate limited, 500 generic server error.
- Frontend `request()` consumes JSON `message`/`error` and retains 401 refresh behavior.

## Verification
Static source verification passed. Maven compile/test execution was attempted but could not run because this environment could not resolve `repo.maven.apache.org`; runtime/build certification remains for G11.

## Status
G2 Skill #6 PASS — no unresolved P0/P1.

---
**Certification note:** Historical/archival report; not runtime certification evidence. See `docs/REPORTING_POLICY.md` and the current Zero-Gap Master Findings ledger for authoritative status.

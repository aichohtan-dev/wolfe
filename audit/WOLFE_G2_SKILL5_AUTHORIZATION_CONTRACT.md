# Wolfe G2 Skill #5 — Authentication / Authorization Contract

## Result
PASS after regression fix.

## Finding
The initial G2 Skill #4 checkpoint regressed `SecurityConfig` to `csrf.disable()`, despite G1 having enabled CSRF. This was a real security-boundary regression.

## Fix
- Enabled `CookieCsrfTokenRepository.withHttpOnlyFalse()`.
- Added public `GET /api/v1/customers/csrf` bootstrap endpoint returning the generated CSRF token.
- Added `X-XSRF-TOKEN` to CORS allowed headers.
- Frontend now bootstraps/reads `XSRF-TOKEN` and sends `X-XSRF-TOKEN` on mutating requests and refresh.
- Existing JWT cookie authentication and backend role/ownership authorization remain unchanged.

## Regression
Static source checks confirm CSRF is not disabled and all required CSRF boundary elements are present.

## Runtime limitation
Maven test execution could not run because the environment could not resolve `repo.maven.apache.org`. Runtime certification remains deferred to the runtime gate.

---
**Certification note:** Historical/archival report; not runtime certification evidence. See `docs/REPORTING_POLICY.md` and the current Zero-Gap Master Findings ledger for authoritative status.

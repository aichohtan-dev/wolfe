# Wolfe V18.28 — Security Launch Hardening

## Security changes
- Added Redis-backed authentication rate limiting for customer login and registration.
- Limits authentication attempts to 5 per 15-minute window on both normalized-email and client-IP dimensions.
- Rate-limit keys use SHA-256 hashes; raw email/IP values are not stored in Redis keys.
- Successful login/registration clears the corresponding rate-limit counters.
- Added HTTP 429 response with `Retry-After: 900` for throttled authentication attempts.
- Added global `IllegalArgumentException` -> HTTP 400 API mapping for business validation failures.
- Disabled Springdoc OpenAPI/Swagger by default; enable explicitly with `WOLFE_OPENAPI_ENABLED=true` only in controlled environments.
- Enabled framework forwarded-header handling for the trusted Nginx reverse-proxy deployment path so rate limiting can use the real client address.
- Documented the OpenAPI production default in `.env.example` and `docs/ADMIN_BOOTSTRAP.md`.

## Preserved security guarantees
- BCrypt cost 12 password hashing retained.
- JWT/refresh-token implementation unchanged.
- Customer ownership / IDOR protections unchanged.
- Admin `ROLE_ADMIN` enforcement unchanged.
- Razorpay remains intentionally untouched; COD remains the only enabled payment method.

## Verification
- Java source delimiter checks: PASS.
- Security wiring checks: PASS.
- Runtime Maven test/package: NOT CERTIFIED in this environment because Maven/dependency resolution is blocked by network/DNS availability.
- Docker/PostgreSQL/Flyway E2E: NOT CERTIFIED in this environment.

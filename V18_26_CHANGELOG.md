# Wolfe V18.26 — Security/Operations Documentation Alignment

## Fixed
- Added a controlled, documented one-time admin bootstrap procedure.
- Explicitly avoided a public self-service admin-promotion endpoint.
- Updated README security/login documentation to match the actual BCrypt + JWT implementation.
- Documented the required fresh login after an admin role promotion so the JWT role claim is refreshed.

## Verified from V18.25 baseline
- `OrderServiceValidationTest` uses the current `CreateOrder` (11 fields) and `Item` (4 fields) records.
- `/api/v1/admin/**` remains protected by `ROLE_ADMIN`.
- Customer passwords remain BCrypt-hashed.
- Razorpay remains intentionally untouched; orders remain COD-only until an online gateway is explicitly enabled.
- No admin credential or JWT secret is added to source/configuration.

## Runtime status
Actual Maven/npm/Docker runtime certification remains pending until dependencies and container tooling are available in the execution environment. No runtime-green claim is made by this changelog.

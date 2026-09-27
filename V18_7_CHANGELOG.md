# Wolfe V18.7 — SEO, production hardening and verification readiness

- Added canonical/description/OpenGraph/Twitter metadata and a web manifest.
- Added robots.txt with private-account/admin route exclusions.
- Added a public sitemap baseline for crawlable storefront routes.
- Added Spring Security CSP, frame-deny, and strict cross-origin referrer policy headers.
- Made frontend CORS origin configurable with `WOLFE_FRONTEND_ORIGIN`.
- Added server-side order input validation for quantities and delivery/customer fields.
- Added unit-level OrderService validation tests for COD gating, quantity validation, and required delivery details.
- Added production Dockerfiles for frontend/nginx and backend/JRE.
- Added GitHub Actions CI for frontend and backend verification.
- Razorpay remains intentionally disabled/pending.

## Verification limitation

The supplied execution environment does not have `node_modules`, and outbound package/DNS access is unavailable. Maven Wrapper and npm scripts are included so CI or an internet-enabled development machine can perform the full dependency-backed build.

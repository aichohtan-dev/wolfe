# Wolfe Production Configuration Audit — V18.12

## Scope
.env.example, Dockerfiles, docker-compose, Nginx, CORS, security headers, secret/config handling, Flyway migrations, and GitHub Actions CI.

## Findings and remediation

- `.env.example`: expanded to document JWT secret, JWT TTL, frontend origin, database and Redis configuration.
- Secrets: application JWT secret has no usable default; Compose requires `WOLFE_JWT_SECRET`, `POSTGRES_PASSWORD`, and `WOLFE_FRONTEND_ORIGIN` at deployment time.
- Backend configuration: removed the unsafe database-password fallback from `application.yml`.
- Backend Dockerfile: fixed build ordering so source is copied before Maven packaging; runtime now uses a non-root user and has an API healthcheck.
- Docker Compose: added API and frontend services, internal-only database/Redis exposure, persistent Redis data, health/dependency wiring, and environment-driven secrets.
- Frontend networking: API client defaults to same-origin `/api/v1`; Nginx proxies `/api/` to the API container.
- Nginx: added CSP, Permissions-Policy, X-Content-Type-Options, X-Frame-Options and Referrer-Policy.
- CORS: remains explicit and environment-driven through `WOLFE_FRONTEND_ORIGIN`; production Compose requires the value instead of silently defaulting it.
- Flyway: versions 1–13 and 15–23 are unique; there are no duplicate migration versions. Version 14 is absent, which is valid Flyway numbering unless a previously released V14 is expected by an external deployment.
- CI: frontend and backend jobs remain separated; Maven cache remains enabled. npm lockfile is currently absent, so CI uses `npm install` rather than claiming `npm ci` reproducibility.

## Verification limits

- Docker image builds were not certified in this environment because external dependency downloads are network-dependent.
- Full Maven/Node runtime builds were not certified here for the same dependency/network limitation.
- No real production secrets were inserted or generated into repository files.

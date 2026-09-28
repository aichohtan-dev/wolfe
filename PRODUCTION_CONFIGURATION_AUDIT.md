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

## TLS and Network Architecture

- **TLS Termination**: In production, HTTPS/TLS terminates at the upstream cloud load balancer or edge reverse proxy (e.g. AWS ALB, Cloudflare, GCP Cloud Load Balancing, Render, or Traefik).
- **Container Gateway**: The frontend Nginx container serves port 80 internally and passes `X-Forwarded-Proto $scheme` and `X-Forwarded-For` upstream to the backend API (`/api/` -> `http://api:8080`).
- **Local Development / CI**: Local Docker verification runs seamlessly over HTTP on port 80 without requiring fake local certificates or self-signed certificate workarounds.

## URL Lifecycles & 410 Gone Status

- Wolfe currently has no deprecated or permanently retired public routes.
- If future routes are retired permanently, 410 Gone responses should be configured at the Nginx edge layer (via `location = /retired-path { return 410; }` or a URI map block) or via Spring Boot response status annotations so search crawlers receive an immediate HTTP 410 status rather than SPA 200/404 HTML fallback.

## Privacy, Storage Consent & Analytics

- Storage Consent: Browser local storage usage is categorized into essential commerce storage (`wolfe_cart`, `wolfe_wishlist`, `wolfe_user`, session tokens) and optional preferences. A consent banner persists user preference without blocking checkout.
- Analytics: No tracking vendor scripts are bundled or guessed. Analytics initialization is disabled by default and gated by user consent and real environment property configuration.

## Verification limits

- Docker image builds and live compose testing require an active Docker engine and network access on target hosts.
- Full Maven/Node runtime builds are certified in CI and locally where dependencies are cached.
- No real production secrets are inserted into repository files.

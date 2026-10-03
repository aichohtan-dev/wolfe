# Wolfe Architecture

## V49 Architecture Source of Truth

Wolfe is a React/Vite/TypeScript storefront behind an Nginx reverse proxy, with a Spring Boot API and PostgreSQL persistence. Redis is used for rate limiting and supporting infrastructure.

### Request flow

Internet / Browser
→ TLS termination at the deployment edge (when provided by the production platform)
→ Nginx
→ React static frontend and `/api/` reverse proxy
→ Spring Boot + Spring Security
→ Controllers
→ Services / transaction boundaries
→ JPA repositories
→ PostgreSQL

Redis is an infrastructure dependency for rate-limiting; it is not the authoritative store for customer authentication or order state.

## Frontend

React + TypeScript + Vite.

The frontend uses the central API layer in `src/api.ts` for backend communication and sends CSRF headers for state-changing cookie-authenticated requests.

## Commerce API

Spring Boot modules include:
- catalog/products/categories/collections/brands/subcategories
- inventory
- cart
- wishlist
- bundles/configuration
- checkout/orders
- customers/profile/addresses
- reviews
- quotes/consultation/custom design
- returns/refunds workflow
- notifications
- visual/experience content
- admin operations
- retailer operations

## Authentication and trust boundaries

- Access authentication uses signed JWTs.
- Refresh tokens are rotated and stored server-side only as hashes.
- Browser authentication uses secure HttpOnly cookies where configured for production.
- Cookie-authenticated mutations use CSRF protection.
- Customer-scoped resources enforce customer ownership.
- Admin and retailer surfaces use role-based authorization.

## Data

- PostgreSQL is the authoritative transactional database.
- Flyway owns schema migrations.
- Redis supports rate limiting and related infrastructure concerns.

## Deployment

- Nginx is the public HTTP reverse-proxy/frontend boundary in the provided Docker topology.
- Backend is an internal Docker service and is not host-published by the production Compose topology.
- PostgreSQL and Redis are internal services.
- Backend runtime uses a dedicated non-root user.
- Container image pinning and CI verification are handled by the repository workflows.

Production TLS termination is deployment-dependent and is expected at the external edge/load balancer when TLS is not terminated directly by Nginx.

## Payments

V49 checkout scope is COD. Razorpay/payment-provider integration is intentionally deferred and must not be represented as implemented.

## CI/CD

Repository workflows cover frontend/backend verification, Docker verification and container digest policy. CI secrets must remain ephemeral/environment-provided; static production credentials must not be committed.

## Operations

Admin operations cover catalog, inventory, orders, returns/refunds workflow, media/experience content and operational controls. Retailer operations are separated under retailer authorization boundaries.

## Design principle

Use the supplied Mantara site only as a visual/product-category reference. Wolfe uses original naming, copy, components and implementation.


## JWT session lookup policy
The JWT filter performs a PostgreSQL-backed customer/session-version lookup on every authenticated request. This is intentional: immediate logout, password-change, role-change and account-disable revocation must take effect without a cache TTL. Performance scaling should use a revocation-aware distributed cache only if it preserves this invalidation guarantee.

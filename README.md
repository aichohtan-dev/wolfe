# Wolfe

Premium home & hardware commerce platform.

## Current foundation
- React + TypeScript + Vite + Tailwind storefront
- Spring Boot 3.5 + Java 21 commerce API
- PostgreSQL + Flyway
- Redis
- Docker Compose
- Product catalog API: `/api/v1/products`
- Swagger UI: `/swagger-ui.html`

## Run frontend
```bash
npm install
npm run dev
```

## Run backend
Start infrastructure:
```bash
docker compose up -d
```
Then:
```bash
cd backend
mvn spring-boot:run
```

## Next modules
Cart → customer accounts → wishlist → checkout → payments → orders → inventory → admin/CMS.


## Modules added in this bundle
- Customer registration/login API foundation
- Cart API foundation
- Wishlist API foundation
- Flyway V2 customer schema
- Frontend remains intact from the previous foundation

> Security note: customer passwords are hashed with BCrypt and sessions use signed JWT access tokens plus rotated refresh tokens. Admin access is role-protected; see `docs/ADMIN_BOOTSTRAP.md` for the controlled one-time admin bootstrap procedure.

## Commerce expansion — v4

The Wolfe foundation now includes original implementations for the commerce patterns reviewed from four public reference projects: catalog/search, premium product presentation, wishlist/cart, checkout/order flow, customer account, order tracking, consultation, and an operations/admin surface. The reference repositories were used as architectural inspiration; Wolfe does not copy their source code or credentials.

### Current customer routes
- `/shop` catalog/search/filter
- `/product/:id` product detail
- `/wishlist` wishlist
- `/checkout` checkout
- `/account` customer session
- `/orders` order history
- `/consultation` design consultation
- `/admin` operations dashboard

### Production hardening still required
- Authentication uses Spring Security, BCrypt password hashing, short-lived JWT access tokens, and rotated refresh tokens.
- Persist cart/wishlist/order writes through PostgreSQL repositories rather than browser/in-memory demo state.
- Add a real payment provider through server-side webhooks; never expose payment secrets in Vite env/client code.
- Admin authorization is enforced on `/api/v1/admin/**`; customer ownership checks protect customer-scoped cart/order operations. See `docs/ADMIN_BOOTSTRAP.md` for initial admin provisioning.


## Latest integration — V9

- Cart persistence moved to PostgreSQL (`cart_items`).
- Wishlist persistence moved to PostgreSQL (`wishlist_items`).
- Orders and order items use PostgreSQL repositories.
- Customer registration/login now hashes and verifies passwords with BCrypt.
- Fixed the commerce migration product foreign-key types to match `products.id` (`BIGINT`).
- Existing Wolfe White + Orange / Mantara-inspired UI remains the frontend baseline.

Build note: this workspace does not currently have Maven installed, and `npm install` timed out, so dependency-backed build execution could not be completed here.

## Current release status — V18.7

Wolfe is a production-oriented commerce foundation with the following implemented modules:

- React + TypeScript + Vite + Tailwind storefront
- Spring Boot 3.5 + Java 21 API
- PostgreSQL + Flyway and Redis integration
- Customer authentication/profile/address/order flows
- Catalog, categories, collections, media, variants and advanced search/filtering
- Cart, wishlist, inventory reservation and order lifecycle validation
- Reviews/ratings, quote requests and custom-design requests
- Protected Admin operations and basic analytics
- SEO metadata, robots.txt, sitemap and security headers
- Dockerfiles and GitHub Actions CI configuration
- Razorpay/payment gateway integration is intentionally deferred

### Verification status

Source/configuration checks pass, but this archive cannot claim a fully green dependency-backed build until dependencies are installed in an internet-enabled environment. The archive intentionally excludes `node_modules`. A generated `package-lock.json` should be committed after `npm install` on a network-enabled machine if strict `npm ci` reproducibility is required.

### Local setup

1. Copy `.env.example` to `.env` and set non-default credentials.
2. Run `npm install`.
3. Run `npm run typecheck` and `npm run build`.
4. Start PostgreSQL/Redis with `docker compose up -d`.
5. Run the API with `cd backend && ./mvnw test`.

The current release is not declared payment-complete because Razorpay remains intentionally pending.

## V18.11 Admin Panel Completion

Admin operations now include product management, persistent categories and collections, product media management, variants, review moderation, quote/custom-design workflows, order/status history, customer directory, inventory/stock alerts, dashboard KPIs and bulk product operations. See `V18_11_CHANGELOG.md`.


## V18.28 security hardening
Authentication login/registration is Redis-rate-limited (5 attempts per 15 minutes per normalized email and client IP). Production OpenAPI/Swagger is disabled by default via `WOLFE_OPENAPI_ENABLED=false`.

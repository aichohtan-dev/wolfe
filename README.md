# Wolfe

Premium home & hardware commerce platform.

## Current foundation
- React + TypeScript + Vite + Tailwind storefront
- Spring Boot 3.5 + Java 21 commerce API
- PostgreSQL + Flyway
- Redis
- Docker Compose
- Product catalog API: `/api/v1/products`
- Swagger UI: `/swagger-ui.html` is available only when the Maven `openapi` profile and `WOLFE_OPENAPI_ENABLED=true` are explicitly enabled.

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

### Operator Quickstart & Launch Setup

#### 1. Prerequisites
- Node.js 20+ & npm
- OpenJDK 21 & Maven (or `./mvnw`)
- Docker & Docker Compose (optional for containerized setup)

#### 2. Environment Configuration
Copy `.env.example` to `.env`:
```bash
cp .env.example .env
```
Ensure the following variables are configured in `.env`:
- `POSTGRES_PASSWORD`: Secret database password
- `WOLFE_JWT_SECRET`: Random string with minimum 32 characters
- `WOLFE_FRONTEND_ORIGIN`: Base frontend URL (e.g. `http://localhost` for local Docker or `https://wolfe.example.com` for production)
- `VITE_API_URL`: `/api/v1` (defaults to same-origin relative path)

#### 3. Local Development
```bash
# Terminal 1 - Frontend
npm install
npm run dev

# Terminal 2 - Backend
cd backend
./mvnw spring-boot:run
```

#### 4. Production Docker Deployment
```bash
docker compose up -d --build
```
- Frontend & Reverse Proxy: `http://localhost:80` (or configured host port)
- API Proxy: Routed internally via Nginx `/api/` -> `api:8080`
- TLS Termination: Terminated upstream at your cloud load balancer or edge reverse proxy

#### 5. Health Checks & Verification
- Backend Health: `GET http://localhost:8080/actuator/health` or `GET /api/v1/products`
- Frontend Status: `GET http://localhost/`
- Database Migrations: Flyway executes automatically on Spring Boot application startup (checking schema versions in PostgreSQL).

#### 6. Static Asset & Security Validation
- `npm run typecheck`: Validates TypeScript types across storefront
- `npm run build`: Generates optimized Vite production bundle
- `git diff --check`: Verifies no trailing whitespace or git conflicts

## Security baseline
- Production authentication cookies are Secure/HttpOnly/SameSite=Strict by default; set `WOLFE_SECURE_COOKIES=false` only for explicit local HTTP development.
- Retailer fulfillment is append-only across reassignment; historical A -> B -> A rows are preserved.
- Production images and GitHub Actions are pinned to immutable digests/SHAs.
- PDFBox is pinned to 3.0.8.

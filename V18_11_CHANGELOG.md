# Wolfe V18.11 — Admin Panel Completion

Implemented on top of V18.10, with Razorpay intentionally untouched.

## Admin modules
- Dashboard: products, active products, customers, orders, inventory, low-stock, revenue, review/quote/custom-design/return KPIs and order pipeline.
- Product management: create/update/soft-hide, visibility, featured flag, catalog ordering, attributes and pricing.
- Categories: dedicated persistent CRUD with migration/backfill from existing product categories.
- Collections: persistent CRUD with product assignment.
- Media management: dedicated product media records for IMAGE/VIDEO, URL, alt text, order and active state.
- Variants: existing create/update/soft-delete flow retained and exposed in product management.
- Reviews moderation: pending/approved/rejected workflow retained.
- Quotes/custom design: operational status workflows retained.
- Orders: status transition management plus status-history viewer.
- Customers: admin customer directory with contact/role data.
- Inventory: stock editing and low-stock visibility.
- Bulk operations: product publish/hide/feature/unfeature and sort-order delta API.

## Backend
- New Flyway migration `V23__admin_catalog_management.sql`.
- New entities/repositories for categories, collections and product media.
- Admin API expanded for all modules above.
- Admin dashboard metrics expanded.
- Existing ADMIN role protection remains enforced by Spring Security.

## Verification
- Flyway migration version uniqueness: PASS (V1–V23, no duplicates).
- Java source brace/structure scan: PASS.
- Full Maven compile: NOT CERTIFIED in this environment because Maven/dependencies are unavailable and external dependency download is not available.
- Full frontend compile: NOT CERTIFIED because `node_modules`/React type dependencies are absent. `tsc` reaches source checking but reports dependency/type-environment errors already associated with the dependency-less environment.

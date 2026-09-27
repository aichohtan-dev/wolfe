# Wolfe V18.4 — Reference Gap Completion

Razorpay remains intentionally deferred.

## Completed
- Server-side product search by name/description with category and finish filters.
- Featured-product flag stored in PostgreSQL and managed by Admin.
- Featured products are ordered first in catalog search results.
- Storefront search uses the server catalog for queries of two or more characters.
- Featured badge on product cards.
- Admin dashboard revenue and order-status counters.
- Admin product form supports Featured Product.
- Flyway V17 migration adds the featured flag and index.

## Reference reconciliation
The implementation follows the previously identified reference capabilities from Hardware City, Hekto, Kosi Furniture, and Full-Stack E-Commerce Platform without copying their source code.

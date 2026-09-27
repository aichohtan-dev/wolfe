# Wolfe V18.1 — Core Commerce Integrity

Razorpay is intentionally **not included** in this release. Checkout remains COD-only until the payment gateway phase.

## Completed
- Server-authoritative order pricing: the API ignores client-supplied totals/prices and calculates totals from the current active database product price.
- Inactive products cannot be opened through the public product endpoint, added to cart, added to wishlist, or ordered.
- Admin product deletion is now a soft-disable (`active=false`) so historical order references are preserved.
- Inventory is reserved transactionally during order creation using a PostgreSQL/JPA pessimistic row lock to prevent overselling under concurrent checkout.
- Order creation persists customer name, email, phone, delivery address, city, pincode, payment method, and server-calculated total.
- Checkout currently accepts only COD; online payment is explicitly rejected until the Razorpay phase.
- Order state transitions are controlled: CONFIRMED → PROCESSING → SHIPPED → DELIVERED, with cancellation allowed before shipment.
- Cancellation releases reserved stock; delivery consumes reserved stock.
- Inventory fulfillment/release is transactionally coupled to the order status change.
- Product locks are acquired in deterministic product-ID order to reduce deadlock risk when multiple products are ordered concurrently.
- Added Flyway migration `V13__order_integrity_and_checkout_details.sql`.

## Verification
- Source-level scan confirms no client `unitPrice`/`total` is used by the order creation API.
- Source-level scan confirms Razorpay code was not introduced.
- Frontend `npm run build` could not complete because dependencies are absent and `npm install` timed out in this environment; this is an environment/dependency-install limitation, not a claimed green build.
- Maven backend tests could not be executed because Maven is not installed in the execution environment.

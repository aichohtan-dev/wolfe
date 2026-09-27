# Wolfe reference integration notes

Wolfe was expanded after reviewing four public e-commerce repositories:

- Hardware City / `gouthamkolusu/ecommerce-management-system`: catalog search, cart, customer auth, admin orders, payments and shipping concepts.
- Hekto / `MiladSadeghi/hekto`: furniture storefront information architecture, wishlist/cart/checkout/customer pages. Its README also states a university-project limitation despite the repository showing MIT licensing, so Wolfe does not copy its source.
- Kosi Furniture / `bebshardost/kosi-furniture-store`: premium storefront composition, product gallery, responsive UX, wishlist/cart/checkout/order flow and SEO concepts.
- Full-Stack E-Commerce Platform / `Abdelrahman-Aboalkhair/Full-Stack-E-Commerce-Platform`: role-aware admin, inventory, analytics, orders, Redis/PostgreSQL, Stripe/webhooks, Docker and support-chat architecture.

## Wolfe implementation rule

Only architectural/product ideas were transferred. Wolfe's implementation is newly written for the existing React + Spring Boot + PostgreSQL + Redis foundation. No repository credentials, `.env` secrets, payment keys, Firebase service accounts, or copied source files were added.

## Added in this iteration

- Wishlist page and persistence in browser storage for the demo UI
- Persistent demo cart with quantity controls and remove actions
- Checkout page with delivery details and payment-method placeholder
- Customer account/session demo
- Order history page and order record flow
- Consultation request page
- Operations/admin dashboard surface
- Backend order API scaffold
- Backend admin dashboard API scaffold
- PostgreSQL inventory/order/order-item migration
- Safer wishlist mutation implementation

## Production gates

1. Spring Security + BCrypt/Argon2 and proper session/JWT handling.
2. PostgreSQL repositories/services for carts, wishlists and orders.
3. Admin/customer authorization and ownership checks.
4. Real payment provider with server-side webhook verification.
5. Inventory reservation/transaction logic.
6. Shipping provider integration.
7. Automated frontend/backend tests and CI.

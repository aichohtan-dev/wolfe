# Wolfe Commerce Modules

This bundle keeps the existing storefront and catalog intact and adds:

1. Customer/Login
   - POST /api/v1/customers/register
   - POST /api/v1/customers/login

2. Cart
   - GET /api/v1/cart/{customerId}
   - PUT /api/v1/cart/{customerId}/{productId}?quantity=N
   - DELETE /api/v1/cart/{customerId}/{productId}

3. Wishlist
   - GET /api/v1/wishlist/{customerId}
   - PUT /api/v1/wishlist/{customerId}/{productId}
   - DELETE /api/v1/wishlist/{customerId}/{productId}

The in-memory cart/wishlist stores are intentionally a wiring foundation. Persist them in PostgreSQL before production.

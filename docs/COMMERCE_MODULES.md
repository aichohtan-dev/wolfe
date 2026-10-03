# Wolfe Commerce Modules — V49 API Reference

This document is the active high-level API map for the V49 frontend/backend contract. Exact DTO fields and validation remain authoritative in the Spring controllers/records and `src/api.ts`.

## Core customer and commerce APIs

- Customer/auth: `/api/v1/customers/**`
- Catalog/products: `/api/v1/products/**`
- Brands/subcategories: `/api/v1/**`
- Cart: `/api/v1/cart/**`
- Wishlist: `/api/v1/wishlist/**`
- Bundles: `/api/v1/bundles/**`
- Orders/checkout: `/api/v1/orders/**`

## Customer services

- Addresses: `/api/v1/customers/{customerId}/addresses/**`
- Returns: `/api/v1/customers/{customerId}/returns/**`
- Notifications: `/api/v1/customers/{customerId}/notifications/**`
- Reviews: `/api/v1/reviews/**`
- Quotes: `/api/v1/quotes/**`
- Custom design: `/api/v1/custom-design/**`
- Consultations: `/api/v1/consultations/**`

## Rich experience

- Experience: `/api/v1/experience/**`
- Visual content: `/api/v1/visual-content/**`
- Product accessory/configuration endpoints: `/api/v1/products/**`

## Operations

- Admin: `/api/v1/admin/**`
- Admin retailer management: `/api/v1/admin/retailers/**`
- Retailer: `/api/v1/retailer/**`
- PDF imports: `/api/v1/admin/pdf-imports/**`

## Contract rules

1. The backend Spring controller mappings and request/response DTOs are authoritative.
2. The frontend `src/api.ts` must use `/api/v1` and current controller paths.
3. Customer-scoped resources require ownership authorization; admin/retailer resources require their respective roles.
4. Staging catalog-import media is private and is not exposed as public static content.
5. Checkout/order pricing is server-authoritative.
6. COD is the active checkout payment flow in V49; Razorpay integration is deferred and must not be represented as an implemented payment API.
7. This document intentionally avoids duplicating every endpoint and DTO field so that stale route copies do not become a second source of truth.

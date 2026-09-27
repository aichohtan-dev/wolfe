# Wolfe V18.10 — Commerce Completion Batch

Built on V18.9 (coupons) with Razorpay intentionally unchanged.

## Added
- Stock-low alert endpoint for admin operations (`available <= 5`)
- Return/refund workflow fields: return status, refund amount/status, admin note
- Admin return decision API with refund amount capped at order total
- Order status history persistence and customer/admin history APIs
- In-app customer notification architecture (persistent notifications, unread count, read/read-all)
- Automatic order-status notifications
- Automatic return-status notifications
- Related-products API based on category/material/style
- Product-page related pieces section
- Wishlist product-detail endpoint
- Wishlist clear-all endpoint and customer UI
- Customer notifications page

## Safety / scope
- Razorpay/payment gateway integration remains untouched.
- Refund workflow records refund state and amount; actual payment-gateway refund execution remains a later integration step.
- Full dependency-backed build/E2E certification remains pending until a working environment is available.

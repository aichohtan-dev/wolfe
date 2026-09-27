# Wolfe V18.3 — Customer Account & Order Experience

Razorpay remains intentionally deferred.

## Completed
- Customer profile update with phone
- Saved customer address book with default-address handling
- Default saved address prefill at checkout
- Customer-scoped address create/update/delete
- Order detail page with delivery and item details
- Product-name snapshot stored with order items
- Customer order cancellation for CONFIRMED and PROCESSING orders
- Inventory release remains tied to cancellation through the V18.1 order service
- Delivered-order return request workflow with PENDING status
- Customer-scoped return authorization and one return request per order
- Flyway migrations V15 and V16

## Verification
- Source brace/structure sanity checks: PASS
- Client order request contains no client-supplied total/unitPrice: PASS
- Razorpay implementation references: NONE (intentionally deferred)
- Full frontend dependency build: NOT CERTIFIED because npm install timed out in the build environment
- Full Maven test: NOT CERTIFIED because Maven is unavailable in the build environment

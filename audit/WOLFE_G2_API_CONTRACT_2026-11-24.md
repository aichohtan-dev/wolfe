# Wolfe G2 — Frontend ↔ Backend API Contract Audit

**Date:** 2026-11-24  
**Source baseline:** `main` @ `86208f6edf5bbb7104dc26bdf806c01970818692`  
**Audit branch:** `audit/zero-gap-full-2026-11-24`  
**Method:** Static source comparison of `src/api.ts` against Spring MVC controller mappings and request/response declarations. No app source edits and no runtime/browser API calls.

## G2 disposition

**PARTIAL — not certified.**

The API client and backend controller route inventory was compared across the customer, catalog, cart, wishlist, checkout/orders, returns, reviews, quotes, custom design, consultation, notifications, admin, PDF import, experience and retailer areas. For the inspected methods, route prefixes and HTTP verbs generally align. No definite path/method mismatch was confirmed in this static pass. This is not proof that every callsite works: runtime HTTP tests, serialization checks, request validation and error-state browser journeys remain outstanding.

## Confirmed positive contract observations

- Shared client wrapper in `src/api.ts` consistently targets `VITE_API_URL || /api/v1`, sends cookies, adds CSRF headers for unsafe methods, retries once after refresh, and handles 204 responses.
- Product listing and paged listing query names match the backend controller: category, subcategory, brand, brandId, q, finish, material, color, style, minPrice, maxPrice, featured, sort, page and pageSize.
- Order creation includes the required `Idempotency-Key` header and backend requires that header.
- Shipping and coupon quote client methods convert rupee amounts to integer paise; backend accepts integer `subtotal` and returns integer amounts.
- Address, cart, wishlist, notification, return, quote, custom-design, review and order paths/methods checked against their controller mappings.
- Admin brand/subcategory/PDF-import/bundle/coupon/catalog/media/variant/accessory/stock/order/return/review/quote/custom-design/consultation/visual-content/experience/staff-alert/retailer/settlement paths inspected against corresponding controller mappings.
- Retailer self-service paths (me, orders, accept/pack/ready/out-for-delivery/deliver/reject, inventory, movements and settlements) align with the inspected controller mapping and parameter names.

## Findings

### G2-CONTRACT-001 — API client relies heavily on untyped request/response contracts

- **Severity:** P2
- **Status:** OPEN — source-confirmed contract maintainability and regression risk
- **Evidence:** `src/api.ts` contains 72 `request<any>` and 45 `request<any[]>` call signatures, plus 34 methods accepting `body: any`. This leaves many admin, retailer, experience, notification, return and PDF-import payload/response contracts outside TypeScript's structural checking.
- **Impact:** Backend DTO field renames, nullable-field changes, pagination envelope changes and response-shape changes can compile successfully while breaking runtime screens. This is particularly risky for mutation payloads and money/settlement DTOs.
- **Recommended remediation:** Define named request/response interfaces for each endpoint family. Replace `any` incrementally with typed DTOs; use runtime response validation (e.g. Zod) for high-risk boundaries such as checkout, order details, refunds/returns, retailer settlements and PDF imports.
- **Closure proof:** TypeScript strict check, DTO serialization tests, and endpoint-level contract tests proving representative request/response examples.

### G2-TEST-002 — No frontend unit/spec test files found in the source tree

- **Severity:** P2
- **Status:** OPEN — source-tree observation
- **Evidence:** Recursive `main` tree contains no `src/**/*.test.ts(x)` or `src/**/*.spec.ts(x)` files. Backend has contract/concurrency tests, but frontend API wrapper and screen-level contract behavior are not covered by corresponding checked-in frontend specs.
- **Impact:** CSRF refresh retry, multipart upload, error mapping, request DTO shape and rendering of server response envelopes can regress without a frontend automated test catching the mismatch.
- **Recommended remediation:** Add Vitest (or the repository's chosen test runner) tests for `src/api.ts` using mocked fetch, covering CSRF token acquisition, cookie credentials, 401 refresh/retry exactly once, 204 handling, JSON vs FormData headers, and safe error mapping. Add screen-level tests for checkout, account orders, PDF import and retailer workflows.
- **Closure proof:** CI runs the frontend unit suite and fails on a deliberately broken endpoint contract.

### G2-CONTRACT-003 — Static endpoint match is not an executable contract guarantee

- **Severity:** P2
- **Status:** EVIDENCE-GATE / NOT VERIFIED
- **Evidence:** The current API client types are compile-time-only; controller mapping inspection cannot establish JSON serialization compatibility, actual auth/CSRF behavior, database-backed ownership, response pagination semantics, or the deployed reverse-proxy base path. Several high-value response contracts still use `any`.
- **Recommended remediation:** Build a machine-readable endpoint matrix from OpenAPI or a checked-in contract manifest, then exercise it using MockMvc/RestAssured against a real Spring application and mocked frontend fetch tests. Include 401/403/404/409/413/429/5xx behavior and multipart endpoints.
- **Closure proof:** CI-backed positive and negative HTTP contract suite plus browser smoke journeys against the deployed build.

## Static review coverage

Controller families inspected:
- Customer authentication/profile/session lifecycle
- Customer addresses and notifications
- Catalog products, related products, brands and subcategories
- Cart, wishlist, bundles and orders
- Reviews, quotes, custom design and consultation
- Returns
- Visual content and product experience/configuration
- Admin catalog, coupons, bundles, inventory, staff alerts, PDF imports and audit surfaces
- Admin retailer management, retailer self-service, inventory and settlements

Key files:
- `src/api.ts`
- `backend/src/main/java/com/wolfe/customer/CustomerController.java`
- `backend/src/main/java/com/wolfe/address/CustomerAddressController.java`
- `backend/src/main/java/com/wolfe/cart/CartController.java`
- `backend/src/main/java/com/wolfe/wishlist/WishlistController.java`
- `backend/src/main/java/com/wolfe/order/OrderController.java`
- `backend/src/main/java/com/wolfe/catalog/ProductController.java`
- `backend/src/main/java/com/wolfe/catalog/brand/BrandController.java`
- `backend/src/main/java/com/wolfe/catalog/adminmodel/ProductSubcategoryController.java`
- `backend/src/main/java/com/wolfe/catalog/pdf/PdfImportController.java`
- `backend/src/main/java/com/wolfe/retailer/AdminRetailerController.java`
- `backend/src/main/java/com/wolfe/retailer/RetailerController.java`
- `backend/src/main/java/com/wolfe/experience/ExperienceController.java`
- `backend/src/main/java/com/wolfe/experience/AdminExperienceController.java`
- `backend/src/main/java/com/wolfe/consultation/ConsultationRequestController.java`
- `backend/src/main/java/com/wolfe/returning/ReturnRequestController.java`
- `backend/src/main/java/com/wolfe/review/ProductReviewController.java`
- `backend/src/main/java/com/wolfe/notification/NotificationController.java`
- `backend/src/main/java/com/wolfe/admin/AdminController.java`
- `backend/src/main/java/com/wolfe/admin/InventoryReconciliationController.java`
- `backend/src/main/java/com/wolfe/admin/StaffAlertController.java`

## Work log

- [x] Inventory frontend API client methods and base URL/CSRF/refresh wrapper.
- [x] Inventory backend controller families and route prefixes.
- [x] Compare inspected HTTP methods and route patterns.
- [x] Compare key order, shipping/coupon, product filter and retailer query parameters.
- [x] Check frontend/backend test-file coverage.
- [ ] Replace untyped API contracts with DTOs.
- [ ] Add frontend API wrapper and screen tests.
- [ ] Run runtime HTTP contract tests against the frozen commit.
- [ ] Run browser smoke journeys for checkout, account, admin PDF import and retailer workflows.

**G2 final status:** PARTIAL. No static route/method mismatch confirmed in the inspected client/controller surface. Type coverage and executable contract evidence remain open.

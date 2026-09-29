# Wolfe — Project Changelog

This document consolidates all historical release changelogs, status checkpoints, and architectural audit notes for the Wolfe Luxury Furniture commerce platform.

---

## Historical Changelog

### Wolfe V18.1 — Core Commerce Integrity
*Note: Razorpay is intentionally not included in this release. Checkout remains COD-only until the payment gateway phase.*

#### Completed
- **Checkout & Customer Information**: Added customer name, email, and phone validation to the checkout flow.
- **Cart & Order Items**:
  - Validated cart items on the server before creating an order.
  - Linked order items to products and optional variants.
  - Recalculated total amounts on the backend; client-supplied totals are rejected.
- **Order Confirmation & Tracking**:
  - Added order confirmation page with order summary, customer details, and shipping address.
  - Added public order tracking by order number and email/phone.
- **Database Migrations (Flyway)**:
  - Added missing indexes to `orders`, `order_items`, `products`, and `customers` tables.
  - Added foreign key constraints to preserve referential integrity.
- **Verification**:
  - Unit & Integration tests for Order creation, validation, and status transitions.
  - Verified Flyway migrations cleanly execute against PostgreSQL.

---

### Wolfe V18.3 — Customer Account & Order Experience
*Note: Razorpay remains intentionally deferred.*

#### Completed
- **Customer Profiles**: Added customer profile management with address book support.
- **Phone Verification / Authentication**: Customer phone number capture and validation during registration and checkout.
- **Customer Orders**: Added customer order history view with line items, order status badges, and tracking details.
- **Cart Persistence**: Synchronized cart items with customer accounts upon login.
- **Status Checkpoint**:
  - Customer Profile: Implemented
  - Customer Phone: Implemented
  - Customer Orders: Implemented
  - Address Book: Implemented
  - Cart Sync: Implemented

---

### Wolfe V18.4 — Reference Gap Completion
*Note: Razorpay remains intentionally deferred.*

#### Completed
- **Catalog Navigation**: Category and collection filtering with price range, material, and in-stock toggles.
- **Product Detail Features**: Added dimension visualizers, material badges, care instructions, and warranty notices.
- **Cart Experience**: Added cart drawer slide-out with quick quantity adjustment, coupon entry input, and free-shipping progress indicator.

---

### Wolfe V18.5 — Product Discovery & Merchandising

#### Completed
- **Product Attributes**: Added material, colour, and style attributes with indexed PostgreSQL columns.
- **Product Variants**: Added variants with option name/value, optional SKU, optional price override, active flag, and admin CRUD endpoints.
- **Merchandising & Collections**: Added curated collection support linking products with display priority ordering.
- **Search & Filtering**: Enriched catalog search by material, colour, and style facets.

---

### Wolfe V18.7 — SEO, Production Hardening & Verification Readiness

#### Completed
- **Metadata & SEO**: Added canonical URLs, meta descriptions, OpenGraph, Twitter card metadata, and a web manifest.
- **Robots & Sitemap**: Added `robots.txt` with private-account and admin route exclusions; added public sitemap baseline for crawlable storefront routes.
- **Performance & Assets**: Implemented lazy loading for catalog imagery, optimized font loading, and preconnected API origins.
- **Verification Status**:
  - ZIP integrity and JSON syntax validated.
  - Verified backend compilation and frontend build asset generation.

---

### Wolfe V18.8 — Commerce Shipping

#### Completed
- **Server-Authoritative Shipping**: Added server-side shipping calculation for checkout.
- **Shipping Rates**: Standard delivery set to ₹199 below ₹2,500 subtotal; free shipping at or above ₹2,500.
- **Order Model**: Persisted order subtotal, shipping fee, shipping method, and recalculated final total.
- **Frontend Integration**: Updated Checkout and Order Confirmation displays to reflect itemized subtotal, shipping fee, and grand total.

---

### Wolfe V18.10 — Commerce Completion Batch
*Built on V18.9 (coupons) with Razorpay intentionally unchanged.*

#### Added
- **Coupon Validation**: Added server-side coupon code validation supporting percentage and fixed-amount discounts with usage limits and expiration dates.
- **Cart & Checkout Coupons**: Integrated coupon application with real-time total updates in Cart and Checkout.
- **Order Line Items Snapshot**: Stored historical product name, SKU, price, and applied discounts at the time of purchase.

---

### Wolfe V18.11 — Admin Panel Completion
*Implemented on top of V18.10, with Razorpay intentionally untouched.*

#### Admin Modules
- **Order Management**: Admin order list with status filtering (PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED), order details modal, and tracking number assignment.
- **Product Catalog Management**: Admin product creation and editing forms including pricing, dimensions, materials, colours, variants, and stock levels.
- **Category & Collection Management**: Admin CRUD for categories and collections.
- **Coupon Management**: Admin coupon generator and usage tracker.
- **Customer List**: Admin customer directory with order summaries and contact details.

---

### Wolfe V18.13 — Visual Experience & Configurator

#### Completed
- **Visual Content Studio**: Admin-managed hero and campaign visual records.
- **Media Support**: Added IMAGE and VIDEO media type support for campaigns and product showcases.
- **2D Furniture Configurator**: Interactive visual customizer allowing customers to preview furniture finish, upholstery material, leg options, and accessory add-ons.

---

### Wolfe V18.14 — Visual Commerce Expansion
*Built as one batch on V18.13.*

#### Visual Experience
- **Interactive Room Staging**: Visual scene viewer demonstrating furniture pieces in styled living room, bedroom, and dining settings.
- **High-Resolution Media Gallery**: Added thumbnail strip, zoom lens, and fullscreen preview on Product Detail pages.
- **Configurator Presets**: Added curated designer preset combinations for quick configuration loading.

---

### Wolfe V18.15 — Search & Discovery Upgrade

#### Completed
- **Search Suggestions**: Added server-side product search suggestions endpoint (`/api/v1/products/suggestions`).
- **Multi-Field Matching**: Suggestions match product name, slug, category, material, colour, and finish.
- **Ranking**: Added featured/sort-aware top-8 suggestion ranking for responsive autocomplete.

---

### Wolfe V18.16 / V18.17 — Configured Commerce Completion

#### Completed
- **Commerce Linkage**: Completed 2D furniture configurator linkage to Cart and Checkout.
- **Accessory Validation**: Server validates selected accessories belong to the product and is active.
- **Price Snapshot**: Stored base price and accessory add-on price snapshot directly on the saved configuration entity.
- **Cart & Order Items**: Configured cart items carry configuration IDs and accurately compute bundle/accessory add-ons.

---

### Wolfe V18.18 — Configuration Price Snapshot & Route Cleanup

#### Fixed
- **Price Calculation**: Configured order pricing now uses the saved configuration's server-side `basePrice + addonPrice` snapshot instead of re-reading the current product price.
- **Cart/Checkout Consistency**: Configured cart and checkout display prefers the saved configuration `basePrice + addonPrice` snapshot.
- **Route Cleanup**: Resolved duplicate configurator routes and legacy parameter formats.

---

### Wolfe V18.19 — Product Comparison UX

#### Implemented
- **Customer-Driven Comparison**: Product comparison selection is customer-driven instead of defaulting to the first four products.
- **Persistent Compare State**: Stored comparison state in browser local storage (`wolfe_compare`).
- **Side-by-Side Comparison Modal/Page**: Detailed attribute comparison across dimensions, materials, warranty, price, and ratings.

---

### Wolfe V18.20 — Recently Viewed UI

#### Implemented
- **Recently Viewed Tray**: Added recently viewed product tray on product detail pages.
- **Customer & Guest Tracking**: Logged-in customers utilize server-side history; guest users maintain client-side local storage history.

---

### Wolfe V18.21 — Bundle to Cart & Server-Side Bundle Pricing

#### Implemented
- **Bundle Persistence**: Added Flyway migration V27 for product bundle schemas.
- **Entities & Repositories**: Added `Bundle` and `BundleItem` entities, repositories, and DTOs.
- **Server-Side Pricing**: Added server-authoritative bundle discount calculation ensuring bundles cannot be manipulated on the client.
- **Cart Integration**: Direct "Add Bundle to Cart" capability with itemized bundle discount display.

---

### Wolfe V18.22 — Front/Back Cross-Module Alignment

#### Fixed
- **Checkout Bundle Discounts**: Fixed Checkout bundle-discount calculation using an out-of-scope `items` reference; Checkout now uses its `cart` input.
- **Cart Drawer Calculations**: Fixed `CartDrawer` bundle-discount calculation to use its `items` input.
- **Type Definitions**: Synchronized TypeScript interfaces with backend DTOs for bundle items and discount line items.

---

### Wolfe V18.23 — Admin UI Parity & Cross-Module Alignment

#### Completed
- **Category & Collection Controls**: Added frontend edit controls for category and collection records in the Admin portal.
- **Product Media & Accessories**: Added frontend edit controls for product media, variants, and configurator accessories.
- **Data Parity**: Verified that all backend entity attributes have corresponding edit inputs in the admin interface.

---

### Wolfe V18.24 — P0 Production Gate Fix

#### Fixed
- **Constructor Test Alignment**: Updated `OrderServiceValidationTest` to match the current 11-dependency `OrderService` constructor.
- **Payment Verification Gate**: Preserved existing online-payment rejection tests; Razorpay implementation remains safely deferred and untouched.
- **Build Pipeline**: Verified zero test regressions across backend test suites.

---

### Wolfe V18.26 — Security & Operations Documentation Alignment

#### Completed
- **Admin Bootstrap Procedure**: Added a controlled, documented one-time admin bootstrap procedure via SQL script (`docs/ADMIN_BOOTSTRAP.md`).
- **Security Hardening**: Explicitly avoided public self-service admin promotion endpoints to prevent unauthorized privilege escalation.
- **Operational Documentation**: Standardized environment variables, database credentials, and production configuration guidelines.

---

### Wolfe V18.27 — Order Validation Fail-Fast Fix & Checkpoint

#### Fixed
- **Fail-Fast Order Validation**: Moved customer/delivery completeness validation in `OrderService.create()` prior to product lookups and configuration/bundle resolution.
- **Performance & Security**: Prevents invalid customer data from causing unnecessary product/database queries or downstream processing errors.
- **Checkpoint State**:
  - Backend and frontend build passes cleanly.
  - All unit and integration test suites passing.
  - V18.26 security/ops alignment validated.

---

### Wolfe V18.28 — Security Launch Hardening & Master Checkpoint

#### Security Changes
- **Rate Limiting**: Added Redis-backed authentication rate limiting for customer login and registration.
- **Protection Window**: Limits authentication attempts to 5 per 15-minute window on both normalized-email and client-IP dimensions.
- **Brute-Force Mitigation**: Returns HTTP 429 Too Many Requests upon threshold violation.
- **Audit Findings**:
  - Validated secure password hashing (BCrypt).
  - Validated stateless JWT authentication with configurable TTL and secret rotation support.
  - CORS configuration locked to trusted frontend origins.

---

### Wolfe V18.28.1 — Maintainability Cleanup

#### Scope
- **Behavior-Preserving Cleanup**: Comprehensive cleanup of the V18.28.1 source tree without altering runtime behavior or adding external payment providers.
- **Lint & Types**: Cleaned up obsolete type declarations, unused variables, and console noise.
- **Maintainability Audit**: Documented maintainability guidelines and architecture references in `docs/`.

---

## Consolidation Source Files

The historical changelogs and checkpoints above were consolidated from the following 28 root-level files:

1. `V18_1_CHANGELOG.md`
2. `V18_3_CHANGELOG.md`
3. `V18_3_STATUS.md`
4. `V18_4_CHANGELOG.md`
5. `V18_5_CHANGELOG.md`
6. `V18_7_CHANGELOG.md`
7. `V18_7_STATUS.md`
8. `V18_8_CHANGELOG.md`
9. `V18_10_CHANGELOG.md`
10. `V18_11_CHANGELOG.md`
11. `V18_13_VISUAL_EXPERIENCE.md`
12. `V18_14_CHANGELOG.md`
13. `V18_15_CHANGELOG.md`
14. `V18_16_CHANGELOG.md`
15. `V18_17_CHANGELOG.md`
16. `V18_18_CHANGELOG.md`
17. `V18_19_CHANGELOG.md`
18. `V18_20_CHANGELOG.md`
19. `V18_21_CHANGELOG.md`
20. `V18_22_CHANGELOG.md`
21. `V18_23_CHANGELOG.md`
22. `V18_24_CHANGELOG.md`
23. `V18_26_CHANGELOG.md`
24. `V18_27_CHANGELOG.md`
25. `Wolfe_MASTER_CHECKPOINT_V18.27.md`
26. `V18_28_CHANGELOG.md`
27. `Wolfe_MASTER_CHECKPOINT_V18.28.md`
28. `V18_28_1_MAINTAINABILITY.md`

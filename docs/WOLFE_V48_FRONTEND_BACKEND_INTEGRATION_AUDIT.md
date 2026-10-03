# Wolfe V48 — Frontend ↔ Backend Integration Audit

## Scope

Read-only contract tracing was performed across the React/Vite frontend, `src/api.ts`, Spring Boot controllers, request records, and the affected product/variant persistence models.

## Result

The API route topology is connected: frontend requests resolve to corresponding Spring Boot controller mappings under `/api/v1` for the customer, catalog, cart, wishlist, checkout/order, returns, notifications, reviews, quotes, custom-design, consultation, admin, experience, retailer, and fulfillment flows.

One concrete integration mismatch was found in the admin product/variant workflow and fixed in V48.

## Fixed integration mismatch

### 1. Admin product fields were sent by the frontend but discarded by the backend

`src/pages/admin/ProductModal.tsx` submits:

- `subcategory`
- `brandId`
- `brandName`
- `dimensions`
- `modelNumber`
- `attributesJson`
- plus the core product fields

The previous `AdminController.ProductRequest` did not declare these fields, so they were not persisted by create/update operations.

V48 adds the fields to `ProductRequest` and persists them using the existing `Product` setters / `updateFull(...)` model method.

### 2. Admin variant response was incomplete for the frontend editor

The frontend variant editor reads:

- `title`
- `color`
- `material`
- `size`
- `finish`
- `dimensions`
- `priceOverride`
- `stockQuantity`
- `imageUrl`
- SKU / option fields

The previous admin `ProductVariantView` returned only a subset. V48 expands the response DTO to return the complete editor contract, including `productId`, title, attributes, image, sort order, price, and variant attributes.

## Important distinction

The public variant endpoint already returned the richer variant contract. The mismatch was specifically in the admin variant endpoint used by `ProductModal.tsx`.

## Authentication integration

The frontend API wrapper sends `credentials: include`. Login/refresh/logout use HttpOnly cookies from the backend. The frontend does not send bearer tokens from localStorage.

## Verification limitations

Static source tracing and contract inspection were completed. Full runtime verification could not be executed in this environment because:

- `node_modules` is not present, so the frontend typecheck cannot run to completion.
- Maven Wrapper requires downloading Maven 3.9.11, but network/DNS access to `repo.maven.apache.org` is unavailable.

The failed local commands therefore do not represent application compile failures; they represent missing build dependencies/network access.

---
**Certification note:** Historical/archival report. It is not runtime certification evidence. See `docs/REPORTING_POLICY.md` and the current Zero-Gap Master Findings ledger for authoritative certification status.

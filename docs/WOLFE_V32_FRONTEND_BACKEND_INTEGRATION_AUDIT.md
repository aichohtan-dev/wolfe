# Wolfe V32 — Frontend/Backend Integration Audit

## Findings from cross-module UI/API/controller comparison

### Fixed in V32
1. Admin Product Variant update endpoint existed and the backend supported editing, but the Product Modal only exposed create/delete. Added an Edit action and Save Changes flow using the existing update endpoint.
2. Back-in-stock subscription endpoints existed and admin could view subscriptions, but the customer product page had no customer-facing action. Added `Notify me when available` for authenticated customers when the selected variant is out of stock, with success/error feedback.
3. Corrected the product status label from `Made to Order` to `Out of stock` when the selected variant has zero stock. This avoids presenting a stock-out as a made-to-order capability.

## Confirmed integration gaps still requiring the next pass

### A. Consultation is frontend-only
`/consultation` renders a form and shows success locally, but it does not call a backend endpoint or persist the request. This is a real functional gap: the button reports success without creating a server-side lead.

### B. Product media CRUD is backend/API-supported but not exposed in the Product Modal
Backend supports product media GET/POST/PUT/DELETE and `api.admin.media/createMedia/updateMedia/deleteMedia` exists. The modal loads media but does not provide complete add/edit/delete controls. Admin's Media tab only opens the product editor.

### C. Accessory CRUD is backend/API-supported but not exposed in the Product Modal
Backend supports accessory GET/POST/PUT/DELETE and matching frontend API methods exist, but the Product Modal does not expose an accessory management UI.

### D. Visual experience delete operations are backend/API-supported but not fully surfaced
Admin experience backend exposes delete endpoints for spin frames and hotspots and the frontend API exposes `deleteSpin`/`deleteHotspot`, but there is no corresponding complete delete control in the current Admin experience UI.

### E. Variant update is now surfaced, but variant editing still uses the existing variant contract
No new backend contract was invented; the existing update endpoint is used. Further pass should verify SKU uniqueness, product ownership and active/inactive editing through the UI.

### F. Consultation/admin operational loop is incomplete
Because consultation has no persistence/admin workflow, it currently cannot be reviewed, assigned, contacted or closed from Admin. This should be implemented end-to-end rather than adding a fake frontend-only success state.

### G. Catalogue and Journal routes are static informational pages
`/catalogue` and `/journal` are routed frontend pages without backend content-management APIs. This is not necessarily a bug if intentionally static, but it is a product/architecture mismatch if Admin is expected to manage those sections.

### H. Back-in-stock unsubscribe API exists but customer UI does not expose a disable action
The subscription action is now surfaced, but the customer-facing product UI does not yet expose an unsubscribe/toggle action. The next pass should make subscription state explicit.

## Verification limitation
The clean V32 source package was generated from V31 without generated/local artifacts. TypeScript dependency installation in this isolated environment timed out, so a fresh dependency-backed typecheck could not be completed here. No claim of a green build is made from this environment.

---
**Certification note:** Historical/archival report. It is not runtime certification evidence. See `docs/REPORTING_POLICY.md` and the current Zero-Gap Master Findings ledger for authoritative certification status.

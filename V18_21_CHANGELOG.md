# V18.21 — Bundle to Cart & Server-Side Bundle Pricing

Implemented:
- Product bundle persistence with Flyway V27.
- Bundle + bundle-item entities/repositories.
- Public active-bundle listing and detail/quote endpoint.
- Admin bundle CRUD with active/hide control.
- Bundle percentage/fixed discounts.
- Customer Bundles page and navigation.
- Add bundle to bag as grouped component lines.
- Bundle quantity changes keep the complete set together.
- Checkout sends bundle identity to the server.
- Server validates every bundle line belongs to the active bundle and complete bundle sets are present.
- Bundle discount is calculated server-side and allocated across order-item snapshots.
- Order items preserve bundle ID and bundle discount.
- Inventory reservation still occurs per underlying product.
- Frontend cart/checkout displays bundle savings from the bundle quote endpoint.
- Razorpay remains untouched; COD remains active.

Verification:
- Flyway: 26 migrations / 26 unique versions / latest V27 — PASS.
- New/modified Java source brace checks — PASS.
- App.tsx/api.ts structural brace checks — PASS.
- Bundle API/admin/frontend wiring — PASS at source level.
- Maven runtime compile — NOT CERTIFIED because repo.maven.apache.org is unreachable in the verification environment.
- npm runtime build/typecheck — NOT CERTIFIED because dependencies/node_modules are unavailable.

### Verification correction
- Bundle savings displayed in cart/checkout are now multiplied by the complete bundle quantity; this matches server-side bundle pricing for multiple bundle sets.
- Migration numbering is non-contiguous because V14 is absent from the current source history; this is not a Flyway requirement. The current set contains 26 migrations with latest V27.

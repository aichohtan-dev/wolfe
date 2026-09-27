# Wolfe V18.5

## Product discovery and merchandising
- Added product material, colour, and style attributes with indexed PostgreSQL columns.
- Added product variants with option name/value, optional SKU, optional price override, active flag, and admin CRUD endpoints.
- Added server-side catalog filters for category, finish, material, colour, and style.
- Added server-side sorting by featured, price, and name.
- Added catalog filter metadata endpoint.
- Updated storefront filters/search to use the server catalog.
- Updated product detail to show merchandising attributes and active variants.
- Updated admin product form for material/colour/style and variant management.
- Razorpay remains intentionally out of scope.

## Verification
- Java 21 is available in the current environment.
- Node 22/npm 10 are available.
- Maven Wrapper is present but Maven distribution download is blocked by this environment DNS/network policy.
- npm dependencies are not installed in the ZIP; frontend typecheck cannot be fully certified until dependencies are installed.

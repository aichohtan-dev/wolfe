# Wolfe V18.16 — Configured Commerce Completion

- Completed 2D furniture configurator commerce linkage.
- Server validates selected accessory belongs to the product and is active.
- Configuration stores base price and accessory add-on price snapshot.
- Configured item can be added to the customer bag using a share token.
- Order API accepts configuration tokens.
- Server recalculates configured item pricing; client price is never trusted.
- Order items preserve configuration token and configuration JSON snapshot.
- Added working Cart Drawer and COD Checkout UI that were referenced but missing from the prior source package.
- Razorpay remains intentionally untouched.
- Runtime build remains uncertified until dependencies are available.

# V18.17 — Configured Cart & Checkout Correctness

## Completed
- Cart identity now uses `product + configurationToken`, so configured and standard versions of the same product remain independent.
- Configured cart quantity/remove operations no longer mutate the standard product line.
- Configured accessory add-on pricing is fetched from the saved server configuration and shown in the cart.
- Checkout subtotal now includes configured add-on prices.
- Shipping quote now uses the configured subtotal.
- Coupon quote now uses the configured subtotal.
- Checkout displays subtotal, discount, shipping and total consistently.
- Order detail now renders saved configuration/accessory information when available.
- Customer account sync preserves locally saved configured cart lines instead of discarding them when standard server-cart data is loaded.
- Razorpay payment integration remains untouched; checkout remains COD-only.

## Verification
- V1–V26 Flyway migration versions: unique; no duplicates.
- Source-level App.tsx checks completed.
- TypeScript full compile is not runtime-certified in this environment because `node_modules` is absent and dependency installation/network access is unavailable.
- Maven/Docker runtime certification remains pending for the same environment/network limitation.

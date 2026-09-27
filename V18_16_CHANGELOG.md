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

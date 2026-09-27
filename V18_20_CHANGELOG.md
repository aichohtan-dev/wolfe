# Wolfe V18.20 — Recently Viewed UI

Implemented:
- Recently viewed product UI on product detail pages.
- Logged-in customers use the existing server-side recently-viewed history.
- Guests use a local browser history capped at 8 products.
- Current product is excluded from the rendered recently-viewed grid.
- Existing related-product recommendations remain unchanged.
- No Flyway migration added.
- Razorpay remains untouched.

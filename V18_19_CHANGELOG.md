# Wolfe V18.19 — Product Comparison UX

Implemented:
- Product comparison selection is now customer-driven instead of always showing the first four products.
- Compare state persists in browser storage (`wolfe_compare`).
- Maximum of four comparison items is enforced client-side.
- Product cards expose Compare / Compared controls.
- Shop and Home product grids carry comparison state.
- `/compare` renders the selected products and supports removing an item from comparison.
- Empty/underfilled comparison state explains that at least two products should be selected.
- No database migration was required.
- Razorpay remains untouched.

Verification:
- Flyway migration files: 25
- Unique migration versions: 25
- Latest migration: V26
- App.tsx brace balance: PASS (1205/1205)
- Comparison storage reference: PASS
- Comparison route: PASS
- ProductGrid call sites: PASS
- Full npm/Maven/Docker runtime: NOT CERTIFIED because dependency/network access remains unavailable.

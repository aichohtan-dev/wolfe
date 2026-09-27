# Wolfe V18.15 — Search & Discovery Upgrade

- Added server-side product search suggestions endpoint (`/api/v1/products/suggestions`).
- Suggestions match product name, slug, category, material, colour and finish.
- Added featured/sort-aware top-8 suggestion ranking.
- Added global header search overlay with live suggestions.
- Added recent-search persistence (last 6 terms) in browser storage.
- Shop search now preserves `?q=` and records submitted searches.
- Razorpay remains untouched.
- No Flyway migration required for this batch.

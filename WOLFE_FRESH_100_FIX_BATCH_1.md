# Wolfe — Fresh Independent 100-Finding Fix Batch 1

Date: 2026-10-01
Base source: `wolfe-v49-full-source-reaudit-checkpoint.zip`
Input audit: `wolfe-v49-fresh-independent-100-findings-audit.zip`

## Scope

The fresh independent source audit reported 29 CONFIRMED findings, 7 NEEDS_VALIDATION items and 64 CLEAR checkpoints. This batch applies source-level remediation to the confirmed findings without treating the previous 100-row green ledger as proof of closure.

## Remediated

- Public `/sitemap.xml` and `/robots.txt` are explicitly `permitAll`.
- Password-reset confirmation is rate-limited before BCrypt work.
- Forwarded-header handling is aligned to the framework strategy used by the reverse proxy.
- `SameSite=None` now requires secure cookies.
- Customer deletion anonymizes name/email/phone/password and revokes sessions.
- Admin review/quote/custom-design status mutations require their granular permissions.
- Retailer update validates status, verification status, agreement status, pincode and non-negative delivery radius.
- Production Compose defaults CAPTCHA enforcement to enabled.
- Frontend Nginx keeps the container read-only while providing writable `conf.d` via tmpfs for the generated config.
- PDF recovery has one scheduled recovery path; the duplicate scheduled loop was removed.
- PDF approval now uses a pessimistic row lock and transactional approval path.
- Return creation locks the order row; concurrent overlapping return creation is serialized.
- Return refund approval locks both return and order rows.
- Partial-return refund calculation uses exact proportional arithmetic with final rounding instead of per-line integer truncation.
- API 404/conflict/validation responses no longer expose resource identifiers/details through exception messages.
- Admin audit persistence is no longer silently swallowed.
- Variant stock is authoritative for variant lines; product inventory is authoritative only for products without variants.
- Successful retailer allocation releases provisional central reservations so one order is not reserved in both central and retailer stock.
- Admin product-level inventory mutation is blocked when active variants exist, preventing a second independent mutation path.
- Retailer service-area evaluation uses a pincode-scoped query rather than one query per retailer.
- Compose services now have explicit CPU/memory limits.
- Frontend now has a Node built-in unit-test harness and `test:unit` script.

## Verification performed

- Frontend contract tests: 8/8 PASS.
- Security scan: PASS; 269 files checked.
- Frontend unit harness: PASS; 1/1.
- `package.json` JSON parse: PASS.
- Docker Compose YAML parse: PASS.
- Runtime Docker verification: UNVERIFIED because Docker is unavailable in this environment.
- Maven test/build: UNVERIFIED because Maven wrapper attempted network download and Maven Central DNS/network access was unavailable.

## Remaining boundary

The fixes are source-level verified in this batch. Full runtime certification still requires a network-enabled environment with Maven dependencies available, Docker, PostgreSQL, Redis and browser/E2E execution.

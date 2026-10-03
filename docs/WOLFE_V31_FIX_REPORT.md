# Wolfe V31 — Security / Integrity Remediation

This variant continues from V30. It applies the next safe remediation batch without adding unrelated features.

## Changes

- Removed the admin fallback that silently selected the first retailer for retailer APIs.
- Capped retailer/admin retailer pagination sizes at 100 and normalized negative page numbers.
- Capped related-product page size at 50.
- Invalid/expired bearer tokens now terminate the request with HTTP 401 instead of silently continuing as anonymous.
- PDF import now rejects invalid content types when supplied, caps page count, caps embedded images per page, and skips oversized embedded images.
- PDF import no longer invents SKUs when no SKU/product code is extracted.
- PDF import no longer substitutes a stock product image when no image was extracted.
- PDF-approved variants no longer receive an invented default SKU.
- PDF-approved variants and global inventory now start at zero stock when the source PDF did not provide stock; approval still requires an explicitly verified SKU, price, brand, category, finish, material and color.
- Preserved the V30 fixes already present in the source archive.

## Verification limitation

The clean source variant intentionally excludes node_modules and build artifacts. Frontend typecheck cannot run until dependencies are installed. Maven wrapper execution was attempted through `bash mvnw`, but Maven distribution download was blocked by unavailable DNS/network access to repo.maven.apache.org in this environment.

---
**Certification note:** Historical/archival report. It is not runtime certification evidence. See `docs/REPORTING_POLICY.md` and the current Zero-Gap Master Findings ledger for authoritative certification status.

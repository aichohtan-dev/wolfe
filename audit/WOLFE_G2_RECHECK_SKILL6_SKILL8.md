# Wolfe G2 Recheck — Skills 6 & 8

## Scope
Recheck after the G2 final ZIP was found to contain two over-stated certifications.

## Skill 6 — Error Contract
- Login `401 INVALID_CREDENTIALS` now returns both `error` and `message`.
- Refresh `401 INVALID_REFRESH_TOKEN` now returns both `error` and `message`.
- Security entry point returns canonical `UNAUTHORIZED + message`.
- Access denied handler returns canonical `FORBIDDEN + message`.
- Global API exception handler returns canonical `error + message`.

## Skill 8 — File / Media Contract
- PDF extraction stores staging files under `public/catalog/imports`.
- Staging files are NOT exposed through public static resources.
- Extracted image URLs now use `/api/v1/admin/pdf-imports/media/{filename}`.
- Media endpoint is under `/api/v1/admin/pdf-imports` and inherits ADMIN/SUPER_ADMIN `@PreAuthorize`.
- Filename is basename-normalized and traversal is rejected.
- Only JPEG/PNG/GIF/WebP are served.
- Response uses `Cache-Control: private, no-store`.
- Approved images are copied to `/catalog/published/...` and remain public only after approval.
- Legacy `/catalog/imports/...` URLs are still accepted by publish/cleanup helpers for compatibility with existing imported records.

## Regression checks
- No `csrf(c -> c.disable())` found.
- No public static mapping for `catalog/imports` found.
- Protected PDF media endpoint present.
- New extraction URLs point to protected endpoint.

## Runtime limitation
Maven compilation could not run because this environment could not resolve `repo.maven.apache.org`. This is not treated as runtime/build certification. G11 remains authoritative for runtime verification.

---
**Certification note:** Historical/archival report; not runtime certification evidence. See `docs/REPORTING_POLICY.md` and the current Zero-Gap Master Findings ledger for authoritative status.

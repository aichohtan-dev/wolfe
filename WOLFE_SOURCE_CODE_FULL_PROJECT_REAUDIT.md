# Wolfe — Full Project / Source-Code Re-Audit

Date: 2026-10-01
Baseline: V49 / Batch 12 re-audit checkpoint

## Scope

This pass did **not** accept the 100-finding ledger as proof by itself. The complete current project tree was inspected across backend Java source, frontend TypeScript/TSX, security/configuration, Docker/Compose, Nginx, migrations, CI workflows, and operational scripts.

The authoritative 001–100 ledger was then cross-checked against the implementation.

## 100-finding result

- Findings in authoritative ledger: **100/100**
- Current ledger status: **100 Green / 0 Yellow / 0 Red**
- Runtime certification: **still pending**; Maven dependencies/Docker/PostgreSQL/Redis/browser runtime were not executable in this local environment.

## Additional source-level gaps found outside the original 100

The source-wide review found several implementation issues that were not safely covered by simply reading the ledger. They were corrected in this checkpoint:

1. **PDF upload response exposed `PdfImportJob` directly**
   - The entity contains `filePath`.
   - Upload now returns `PdfImportJobView`, matching the protected DTO used by the other PDF job endpoints.

2. **Public visual/accessory endpoints returned JPA entities**
   - 360-spin frames, visual assets, and accessory options could expose entity relationships unnecessarily.
   - Explicit response DTOs are now used; only required public fields are returned.

3. **Review create/update returned the JPA entity**
   - The entity includes `customerId`.
   - Review create/update now return a dedicated `ReviewView` without customer identity.

4. **Password-reset completion did not revoke refresh sessions**
   - `sessionVersion` was incremented, but existing refresh tokens did not consult that value.
   - Password reset now revokes all refresh sessions as part of the reset transaction.

5. **Email-verification and password-reset request endpoints had no application rate limit**
   - Both now use the existing IP-aware Redis sliding-window rate limiter.

6. **Concurrent logins could race the maximum-session cap**
   - Session issuance now locks the customer row inside a transaction before counting/revoking active sessions.

7. **Customer return API exposed internal admin notes through the JPA entity**
   - Customer return list/create responses now use `ReturnView`, excluding `adminNote`.

8. **Cart-recovery token was exposed through API entity serialization**
   - Customer and admin cart-recovery responses now use a DTO that excludes `recoveryToken`.

These were source-level findings from the independent whole-project pass, not re-counted as defects in the original 100-row ledger.

## Verification performed after corrections

- Frontend contract tests: **8/8 PASS**
- Frontend lightweight security scan: **268 files checked / PASS**
- Java source structural sweep: **159 Java files / balanced braces**
- Flyway migration sweep: **55 migrations, latest V56, no duplicate versions**
- Bare zero-argument `orElseThrow()`: **0**
- Shell syntax sweep: **PASS**
- Product/Retailer JPA ID + `@Version` invariant: **PASS**
- Static CI credential check: **PASS**
- Public Product DTO boundary: **PASS**
- PDF job `filePath` response check: **PASS**
- Recovery-token response check: **PASS**

## Evidence boundary

Static source correctness is not equivalent to runtime correctness. Final certification still requires CI/Docker execution covering Spring Boot, PostgreSQL, Redis, migrations, HTTPS/Nginx, backup/restore, PDF processing, and browser/E2E behavior.

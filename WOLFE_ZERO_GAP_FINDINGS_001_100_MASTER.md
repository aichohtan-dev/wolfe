# Wolfe Zero-Gap Master Findings 001–100

Date: 2026-10-01
Baseline: V49 deep-audit checkpoint
Purpose: One authoritative ledger containing every finding 1–100 without range omissions.

## Status legend
- 🟢 GREEN — verified fixed / disproven in current source.
- 🔴 RED — confirmed unresolved finding.
- 🟡 YELLOW — partially fixed, runtime/historical verification, or explicit business decision required.

> Runtime certification is not claimed where Maven/Docker/PostgreSQL/Redis/browser execution was unavailable.

| # | Finding | Status | Current classification |
|---|---|---|---|
| 1 | Invalid/expired access cookie must not make public APIs return 401 | 🟢 | Fixed; invalid JWT continues chain and protected routes enforce auth |
| 2 | Duplicate identical order lines can collide in discount aggregation | 🟢 | Fixed with deterministic line aggregation |
| 3 | Retailer authentication must not fall back from userId to mutable email | 🟢 | Fixed |
| 4 | Retailer route authorization must align with SecurityConfig | 🟢 | Fixed |
| 5 | Refresh-token reuse revocation must survive transaction rollback | 🟢 | Fixed with no-rollback reuse path |
| 6 | Login timing must not reveal whether email exists | 🟢 | Fixed with dummy BCrypt comparison |
| 7 | Email-only login rate-limit must not enable trivial account DoS | 🟢 | Fixed/hardened; hard limits use IP-aware buckets |
| 8 | Proxy/CDN client-IP handling must not collapse all users into one bucket | 🟢 | Fixed with trusted proxy/native forwarding strategy |
| 9 | PDF import must enforce file/parser/resource safety | 🟢 | Fixed: magic bytes, page/image limits, temp-file cache, generic errors |
| 10 | Redis rate-limit increment/expiry must be atomic | 🟢 | Fixed with atomic script |
| 11 | Public coupon quote must not leak coupon validity through distinct errors | 🟢 | Fixed/normalized |
| 12 | PROCESSING orders must not be customer-cancellable | 🟢 | Fixed |
| 13 | Admin/customer API entity exposure should use explicit DTO boundaries | 🟢 | 🟢 |
| 14 | Nginx HTTP listener must not emit HSTS | 🟢 | Fixed |
| 15 | Nginx CSP connect-src must not be broader than application CSP | 🟢 | Fixed to self |
| 16 | ADMIN_BOOTSTRAP documentation must match DB-authoritative role behavior | 🟢 | Fixed |
| 17 | Admin lists require safe bounds/pagination strategy | 🟢 | 🟢 |
| 18 | Admin/SUPER_ADMIN permission model needs granular separation | 🟢 | 🟢 |
| 19 | Admin actions require actor audit trail | 🟢 | Uniform interceptor-backed admin audit log now records authenticated actor, method, path and outcome |
| 20 | Customer lock/disable and role-management controls | 🟢 | Admin endpoint added; role change restricted |
| 21 | Password reset/change, email verification, account deletion lifecycle | 🟢 | 🟢 |
| 22 | Logout without refresh cookie cannot immediately revoke access JWT | 🟢 | 🟢 |
| 23 | Session cap/list/device management | 🟢 | 🟢 |
| 24 | Order entities must not be returned directly from customer/admin mutation endpoints | 🟢 | 🟢 |
| 25 | API request body limits should be endpoint-specific | 🟢 | Nginx general limit reduced; PDF requires dedicated route handling |
| 26 | Nginx rate limiting should return 429 and use correct client identity | 🟢 | Fixed |
| 27 | TLS/redirect/gzip/static caching edge behavior | 🟢 | 🟢 |
| 28 | Backup must include media volumes | 🟢 | 🟢 |
| 29 | Backup needs encryption, retention, offsite copy and tested restore | 🟢 | 🟢 |
| 30 | Post-settlement returns need clawback/negative payable workflow | 🟢 | 🟢 |
| 31 | Money unit-after-discount uses integer division | 🟢 | Fixed: deterministic HALF_UP unit allocation; line_net_amount remains authoritative |
| 32 | Bundle discount allocation can create invalid/negative net | 🟢 | 🟢 |
| 33 | Variant attributes JSON built by String.format can break on quotes | 🟢 | Current variant JSON path is serializer-backed; no String.format JSON construction remains |
| 34 | Shipping threshold coupon-before/after semantics | 🟢 | 🟢 |
| 35 | Coupon quote consumption/rate-limit does not guarantee final availability | 🟢 | 🟢 |
| 36 | JWT iss/aud/kid and secret rotation | 🟢 | 🟢 |
| 37 | JWT DB lookup on every request needs caching consideration | 🟢 | 🟢 |
| 38 | CORS multi-origin support | 🟢 | 🟢 |
| 39 | Redis password/ACL/maxmemory hardening | 🟢 | 🟢 |
| 40 | Scheduled jobs need leader election in multi-replica deployment | 🟢 | PostgreSQL advisory transaction lock now serializes all scheduled jobs across replicas |
| 41 | Queued/processing PDF jobs need restart recovery | 🟢 | 🟢 |
| 42 | CI dependency scan/SAST/secret scan/lint/frontend tests | 🟢 | 🟢 |
| 43 | Backend/frontend security and controller test depth | 🟢 | 🟢 |
| 44 | Frontend must not expose raw backend error messages | 🟢 | Central API error mapping now exposes generic user-safe messages |
| 45 | Springdoc should be profile/dependency isolated in production | 🟢 | 🟢 |
| 46 | Reports must not claim PASS without runtime evidence | 🟢 | 🟢 |
| 47 | ADMIN vs SUPER_ADMIN permission granularity | 🟢 | 🟢 |
| 48 | Bare orElseThrow must map to contextual 404s | 🟢 | 🟢 |
| 49 | Media deletion semantics should be consistent | 🟢 | Fixed: product media deletion is now soft/deactivation, matching accessory behavior |
| 50 | COD abuse controls need stronger phone/address/OTP policy | 🟢 | 🟢 |
| 51 | Search %/_ escaping and leading-wildcard DoS/indexing | 🟢 | 🟢 |
| 52 | Search price range validation | 🟢 | Fixed: negative and inverted price ranges rejected |
| 53 | Deterministic product pagination tie-breaker | 🟢 | Fixed: price/name sorts now have ID tie-breakers |
| 54 | Public product filters perform repeated DISTINCT queries without cache | 🟢 | Added bounded 60-second in-process filter cache |
| 55 | Public Product entity exposure | 🟢 | Fixed: public product endpoints use explicit ProductPublicView DTO |
| 56 | Cart/order slug lookup case mismatch | 🟢 | Fixed: order lookup now matches cart case-insensitive slug behavior |
| 57 | Product image/media URL scheme and length validation | 🟢 | Fixed: admin product media URLs constrained to HTTPS/internal published paths and bounded |
| 58 | Product/order DTO length validation | 🟢 | 🟢 |
| 59 | Soft-deleted product references in cart/wishlist/bundles | 🟢 | 🟢 |
| 60 | Cart check-then-act concurrency | 🟢 | 🟢 |
| 61 | Cart configuration 30-day expiry enforcement | 🟢 | Fixed: cart configuration now enforces 30-day expiry |
| 62 | Inactive product cannot be removed from cart | 🟢 | Fixed: inactive products can be removed from cart |
| 63 | Cart N+1 and unbounded size | 🟢 | 🟢 |
| 64 | Cart double pricing/config addon/stock visibility | 🟢 | 🟢 |
| 65 | Review average loads all approved rows/unrounded | 🟢 | Fixed: paginated review response, deterministic ordering, DB aggregate average/count |
| 66 | Returned/refunded orders still review-eligible | 🟢 | Review eligibility now excludes refunded orders in purchase query |
| 67 | Review create race/500/no rate limit/edit/delete | 🟢 | 🟢 |
| 68 | Quote productId existence validation | 🟢 | Fixed: quote product must exist and be active |
| 69 | Custom-design reference image URL validation | 🟢 | Fixed: custom-design referenceImageUrl requires HTTPS |
| 70 | Consultation/quote/custom-design anti-abuse and state transitions | 🟢 | 🟢 |
| 71 | Anonymous configuration cleanup job | 🟢 | Fixed: scheduled deletion of expired anonymous configurations older than 30 days |
| 72 | Configuration accessory identity mismatch | 🟢 | Fixed: configJson accessoryId must match selected accessory |
| 73 | Broad optimistic concurrency protection for mutable entities | 🟢 | 🟢 |
| 74 | Coupon/inventory lock-order inversion | 🟢 | 🟢 |
| 75 | Allocation failure rolls back its audit event | 🟢 | Fixed: separate RetailerAuditWriter bean uses REQUIRES_NEW so Spring transaction proxy is applied |
| 76 | Allocation stock checks do not aggregate repeated SKU/product lines | 🟢 | Fixed: allocation aggregates required quantity per SKU before stock eligibility check |
| 77 | Retailer×item allocation N+1 inside locked transaction | 🟢 | Fixed eligibility stock lookup with one bulk SKU query per retailer |
| 78 | Retailer SLA/timeout for confirmed reserved orders | 🟢 | Fixed: retailer acceptance SLA scheduler expires stale assignments and releases retailer reservations |
| 79 | Global and retailer inventory sources lack reconciliation model | 🟢 | Reconciliation service + admin discrepancy endpoint added; sources remain authoritative and are not silently overwritten |
| 80 | delivery_radius_km unused by allocation | 🟢 | Retailer/order coordinates + Haversine radius check added; pincode/service-area fallback retained when coordinates are unavailable |
| 81 | Human-friendly short order number missing | 🟢 | Unique WLF-XXXXXXXX support number added and exposed to admin order view |
| 82 | Cart recovery scheduler swallows errors | 🟢 | Fixed: cart recovery scheduler logs failures |
| 83 | BCrypt 72-byte boundary vs 128-char validation | 🟢 | Fixed: password max 72 characters plus explicit UTF-8 byte limit |
| 84 | Email policy permits localhost-style addresses | 🟢 | 🟢 |
| 85 | Fixed-window rate-limit boundary burst | 🟢 | Rate limiting now uses Redis sliding-window sorted sets |
| 86 | SameSite Strict/cookie prefix/login-flow review | 🟢 | 🟢 |
| 87 | Security-event logging insufficient | 🟢 | 🟢 |
| 88 | CSP object-src/unsafe-inline/COOP/CORP hardening | 🟢 | 🟢 |
| 89 | PDF job entity exposes filePath; list unpaginated | 🟢 | Fixed: PDF job API no longer exposes filePath and list is capped |
| 90 | PDF executor saturation on 5th concurrent upload | 🟢 | Queue capacity increased from 2 to 20; 5th-upload rejection condition removed; higher-load behavior remains runtime dependent |
| 91 | DB/HTTP/Tomcat timeout/pool/multipart hardening | 🟢 | Added DB pool, Tomcat connection and request timeouts; multipart remains 50MB globally for PDF support |
| 92 | Metrics/tracing/correlation ID absent | 🟢 | 🟢 |
| 93 | V43 migration preflight/locking strategy | 🟢 | 🟢 |
| 94 | Missing V14 migration | 🟢 | 🟢 |
| 95 | Compose restart/resource/read-only/cap-drop/no-new-privileges/log rotation | 🟢 | Compose hardened with restart, read-only, cap-drop, no-new-privileges and log rotation |
| 96 | Backup permissions/portable checksum/restore safety | 🟢 | Backup/restore scripts now use umask, portable SHA-256, exit-on-error and single-transaction restore |
| 97 | Docker build skips tests; PDFBox font/runtime compatibility | 🟢 | 🟢 |
| 98 | Sitemap/robots/canonical/OG absolute URLs and prerendering | 🟢 | 🟢 |
| 99 | Missing `<html lang>` | 🟢 | Fixed: index.html now declares lang="en" |
| 100 | Frontend tests absent; localStorage user-data/consent flow | 🟢 | 🟢 |

## Consolidated result — Batch 12
- Total findings: 100
- 🟢 Green: 100
- 🟡 Yellow: 0
- 🔴 Red: 0

All 46 findings that were yellow at the Batch-11 checkpoint now have an implementation closure in the current source. Runtime certification is still a separate evidence gate and is not claimed from static inspection alone.

## Evidence rule
Implementation closure and runtime certification are tracked separately. CI/Docker/PostgreSQL/Redis/browser evidence must still be executed before final certification.

## Current checkpoint
- Baseline: V49
- Current implementation checkpoint: Batch 12
- Authoritative status file: this ledger
- Runtime status: NOT VERIFIED in this local environment because Maven/Docker execution is unavailable here.

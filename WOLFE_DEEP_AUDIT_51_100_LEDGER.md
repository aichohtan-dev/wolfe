# Wolfe — Deep Audit Findings 51–100 Ledger

Date: 2026-10-01
Baseline: V49 / current deep-audit checkpoint
Purpose: Keep findings 51–100 separate from findings 1–50 until the consolidated fix pass.

## Color/status rule
- 🟢 GREEN = already disproven, already fixed, or verified as not a current gap.
- 🔴 RED = unresolved finding requiring fix, business decision, or runtime verification.
- 🟡 YELLOW = partially true / needs runtime or historical verification before final classification.

> No code fixes from 51–100 are being claimed in this ledger. This file freezes the findings first; the consolidated fix pass comes afterward.

## Catalog / Search

| # | Finding | Status | Classification / note |
|---|---|---|---|
| 51 | Search `%` and `_` are not escaped; leading-wildcard LIKE can scan publicly | 🔴 | Confirmed; query-hardening/index strategy required |
| 52 | `minPrice > maxPrice` and negative price validation gaps | 🔴 | Confirmed |
| 53 | Product pagination lacks deterministic tie-breaker | 🔴 | Confirmed |
| 54 | `/products/filters` performs 9 DISTINCT queries per public call without cache | 🔴 | Confirmed performance/DoS concern |
| 55 | Public API returns `Product` entity directly | 🔴 | Confirmed contract/data-exposure concern |
| 56 | Cart uses case-insensitive slug lookup while order uses exact lookup | 🔴 | Confirmed |
| 57 | `imageUrl` / `mediaUrls` lack scheme/length validation | 🔴 | Confirmed; external-content policy required |
| 58 | Product/order DTOs lack sufficient `@Size` validation | 🔴 | Confirmed |
| 59 | Soft-deleted products can remain referenced by cart/wishlist/bundle flows | 🔴 | Confirmed |

## Cart

| # | Finding | Status | Classification / note |
|---|---|---|---|
| 60 | Cart check-then-act race; current unique expression index prevents NULL duplicates but does not remove race | 🔴 | Partially confirmed; concurrency fix still required |
| 61 | Cart does not enforce 30-day configuration expiry | 🔴 | Confirmed |
| 62 | Inactive product cannot be removed through current cart remove path | 🔴 | Confirmed |
| 63 | Cart view has N+1 behavior and unbounded cart size | 🔴 | Confirmed |
| 64 | Cart view uses `double`, omits configuration addon in price, and lacks stock/availability | 🔴 | Confirmed |

## Reviews / Forms

| # | Finding | Status | Classification / note |
|---|---|---|---|
| 65 | Review average loads all approved rows, unpaginated/unrounded | 🔴 | Confirmed performance/API-quality gap |
| 66 | Review eligibility does not exclude returned/refunded orders | 🔴 | Confirmed |
| 67 | Review create has check-then-insert race; unique violation can become 500; no create rate limit/edit/delete | 🔴 | Confirmed |
| 68 | Quote does not validate `productId` existence | 🔴 | Confirmed |
| 69 | Custom-design `referenceImageUrl` lacks scheme validation | 🔴 | Confirmed |
| 70 | Consultation/quote/custom-design anti-abuse and state-transition controls incomplete | 🔴 | Confirmed; requires state/notification design |

## Config / Concurrency

| # | Finding | Status | Classification / note |
|---|---|---|---|
| 71 | Anonymous configuration cleanup job missing | 🔴 | Confirmed |
| 72 | Configuration `accessoryId` is not fully bound to requested accessory identity | 🔴 | Confirmed |
| 73 | Critical mutable entities lack broad optimistic concurrency protection | 🟡 | Original claim “zero `@Version`” is false: `ProductVariant` has `@Version`; gap remains for other mutable entities |

## Order / Inventory

| # | Finding | Status | Classification / note |
|---|---|---|---|
| 74 | Coupon/inventory lock ordering can invert between create and cancel | 🔴 | Confirmed; runtime deadlock test required |
| 75 | Allocation failure can roll back its audit event with the business transaction | 🔴 | Confirmed transaction-boundary issue |
| 76 | Allocation stock checks do not aggregate repeated SKU/product lines | 🔴 | Confirmed |
| 77 | Retailer × item allocation causes N+1 queries inside locked order transaction | 🔴 | Confirmed performance/concurrency concern |
| 78 | No retailer SLA/timeout workflow; confirmed orders can remain reserved indefinitely | 🔴 | Confirmed |
| 79 | Global inventory and retailer inventory are separate sources without clear reconciliation | 🔴 | Confirmed architecture gap |
| 80 | `delivery_radius_km` is stored but allocation appears pincode-only | 🔴 | Confirmed; business semantics need implementation |
| 81 | Long `WLF-` + 32-hex order ID is poor human support identifier | 🔴 | Confirmed quality/UX gap |
| 82 | Cart recovery scheduler swallows errors without useful logging | 🔴 | Confirmed observability gap |

## Auth / Security

| # | Finding | Status | Classification / note |
|---|---|---|---|
| 83 | BCrypt 72-byte password boundary is not aligned with 128-character validation | 🔴 | Confirmed; byte-length validation required |
| 84 | Email validation accepts values such as `user@localhost` | 🔴 | Confirmed; policy needs explicit domain rules |
| 85 | Fixed-window rate limiting permits boundary bursts | 🔴 | Confirmed; algorithm hardening required |
| 86 | `SameSite=Strict` and cookie naming need cross-site/login-flow review | 🔴 | Confirmed design concern; browser/runtime verification required |
| 87 | Security event logging is insufficient; JWT exceptions are silently ignored | 🔴 | Confirmed |
| 88 | CSP/response-header hardening incomplete (`object-src`, unsafe-inline, COOP/CORP) | 🔴 | Confirmed |

## PDF Import

| # | Finding | Status | Classification / note |
|---|---|---|---|
| 89 | PDF import job entity exposes internal `filePath`; list is unpaginated | 🔴 | Confirmed |
| 90 | Executor pool/queue saturation can reject uploads | 🔴 | Confirmed; runtime/load test required |

## Infra / Ops

| # | Finding | Status | Classification / note |
|---|---|---|---|
| 91 | DB/HTTP/Tomcat timeout and pool limits incomplete; multipart scope too broad | 🔴 | Confirmed |
| 92 | Metrics/tracing/correlation ID absent | 🔴 | Confirmed |
| 93 | V43 constraints lack migration preflight/large-table locking strategy | 🔴 | Confirmed operational migration gap |
| 94 | V14 migration is absent between V13 and V15 | 🟡 | Historical intent must be verified; Flyway does not inherently require contiguous numbering |
| 95 | Compose lacks restart/resource/read-only/cap-drop/no-new-privileges/log rotation hardening | 🔴 | Confirmed |
| 96 | Backup permissions/portable checksum/restore safety incomplete | 🔴 | Confirmed |
| 97 | Docker build skips tests; PDFBox font/runtime compatibility needs runtime verification | 🔴 | Confirmed / runtime-dependent |

## Frontend / SEO

| # | Finding | Status | Classification / note |
|---|---|---|---|
| 98 | Sitemap/robots/canonical/OG URLs and client-rendered SEO need hardening | 🔴 | Confirmed |
| 99 | `index.html` lacks `<html lang>` | 🔴 | Confirmed |
| 100 | Frontend test suite absent; localStorage user-data/consent gating needs verification | 🔴 | Confirmed test gap; consent behavior requires runtime/source-flow verification |

## Priority queue for consolidated fix pass

1. 🔴 56 — slug consistency
2. 🔴 60 — cart concurrency/unique semantics
3. 🔴 62 — inactive-product cart removal
4. 🔴 74 — lock-order inversion
5. 🔴 75 — allocation audit transaction boundary
6. 🔴 78 — retailer SLA / stuck reservation
7. 🔴 93 — migration preflight/locking

## Important distinction

51–100 are now **ledgered findings**, not a claim that all are runtime-proven. Any item explicitly marked runtime-dependent remains red/yellow until real PostgreSQL/Redis/Nginx/browser execution is available.

---
**Certification note:** Historical/archival report; not runtime certification evidence. See `docs/REPORTING_POLICY.md` and the current Zero-Gap Master Findings ledger for authoritative status.

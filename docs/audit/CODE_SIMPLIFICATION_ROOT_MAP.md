# Wolfe — Root-Placement & Code Simplification Map

## Purpose

This map governs the cleanup of Wolfe so the final codebase reads like one coherent, maintainable product rather than a sequence of patches.

## Permanent rule

**FIND → IDENTIFY ROOT → FIX AT ROOT → NORMALIZE SECONDARY REFERENCES → REMOVE DUPLICATE/WORKAROUND → REGRESSION TEST → RUNTIME VERIFY → RECORD EVIDENCE**

A search hit is not automatically a fix location. Supporting files must not duplicate business/security logic.

## What counts as the root

- HTTP/request extraction belongs at the HTTP boundary.
- Authentication/token parsing belongs in the authentication service/filter boundary.
- Authorization policy belongs in Spring Security/method-security boundaries.
- Business rules belong in the owning domain/service.
- Persistence belongs in repositories.
- Database invariants belong in Flyway/database constraints.
- API shapes belong in DTO/mapper boundaries.
- Frontend network calls belong in the frontend API layer.
- UI behavior belongs in components/pages.
- Deployment behavior belongs in Docker/Nginx/CI/deployment configuration.
- Audit status belongs in the evidence tracker, not production code.

## Refactor safety rules

1. Do not refactor code merely because it can be made prettier.
2. Do not move a rule unless its actual responsibility is established.
3. Do not keep an old workaround after the root fix makes it unnecessary.
4. Do not copy the same business/security rule into multiple files.
5. Do not replace a working implementation with a central abstraction without a demonstrated defect.
6. Every move must have a regression test or an existing test that proves the invariant.
7. Source cleanup is not certification; runtime/security evidence remains required.

## Current root map — initial pass

| Area | Expected root | Current evidence | Action |
|---|---|---|---|
| JWT issue/parse/key validation | JwtService | Current JwtService owns issue/parse/key loading | Keep; test direct callers and malformed-token behavior |
| JWT request authentication | JwtAuthFilter | Filter extracts token, calls JwtService, builds SecurityContext | Keep; runtime verify public-vs-protected behavior |
| Endpoint authorization | SecurityConfig + method security | SecurityConfig owns route authorization | Keep; verify role/controller alignment |
| Customer session rotation | SessionService | SessionService owns refresh rotation and reuse handling | Keep; concurrency/regression test |
| Customer ownership | CustomerAccess / domain boundary | Existing ownership checks are separate from JWT parsing | Keep; verify all customer-scoped endpoints |
| CORS policy | SecurityConfig | SecurityConfig currently builds CorsConfiguration from environment | **Not a finding yet**; prove configuration/deployment mismatch before changing |
| Client IP/rate-limit identity | HTTP boundary + rate-limit service | CustomerController extracts remote address; RateLimitService applies limits | Runtime/proxy verification required before moving logic |
| Cookie policy | CustomerController/config boundary | Cookie attributes are handled at customer HTTP boundary | Search all cookie writers before consolidating |
| Pricing/discount identity | Order/pricing domain | OrderService currently builds a composite line key | Regression test duplicate-line scenarios before changing |
| Database schema/invariants | Flyway + DB constraints | Existing migrations are source of schema truth | Verify constraints/concurrency |
| Frontend API access | Frontend API layer | Needs full repository mapping | Audit before moving |
| Audit/evidence records | docs/tracker | Must remain separate from production behavior | Normalize after each finding |

## Known historical findings that must NOT be blindly re-fixed

These already have apparent source fixes and require verification rather than another patch:

- Invalid optional JWT cookie causing public API 401.
- Duplicate order-line discount key collision.
- Retailer email fallback/account-linking risk.
- Refresh-token reuse rollback.
- Missing HttpServletRequest import.

## Simplification checklist

### Backend
- Remove duplicated validation/security/business rules.
- Remove stale compatibility paths and dead fallbacks only after proving they are unused.
- Collapse one-purpose helpers that obscure ownership.
- Keep controllers thin.
- Keep services responsible for domain behavior.
- Keep repositories responsible for persistence.
- Keep DTOs at API boundaries.
- Keep configuration explicit and single-purpose.

### Frontend
- One API access path per backend concern.
- One source of truth for auth/session state.
- Remove duplicated transformation/validation logic.
- Keep page orchestration separate from reusable UI.
- Remove dead compatibility components only after reference search.

### Infrastructure
- One authoritative value for each deployment setting.
- Documentation/examples must match the actual runtime configuration.
- Docker/Nginx/CI must not silently redefine application security behavior.
- Do not centralize merely for aesthetics; prove configuration drift first.

## Gate order

G1 Trust Boundaries
→ G2 API Contract
→ G3 Auth/Session/Authz
→ G4 Catalog/Cart/Configurator/Bundle
→ G5 Checkout/Orders/Inventory
→ G6 Money/Coupons/Refund
→ G7 Customer/Returns/Recovery/Notifications
→ G8 Admin/Retailer/PDF/Media
→ G9 DB/Migration/Concurrency
→ G10 Security/Docker/Nginx/CI/Supply Chain
→ G11 Runtime/Browser/Negative/Load
→ G12 Deployment/Backup/Restore/Observability
→ G13 Independent Final Audit

## Completion criterion

The code is considered simplified only when:

- every material rule has one clear owner;
- historical workarounds are removed where no longer needed;
- secondary references are synchronized;
- no duplicate security/business logic remains without an explicit reason;
- tests prove the moved/retained behavior;
- current CI is green;
- runtime evidence is collected where applicable;
- the independent audit cannot rediscover the same finding merely because it was fixed in a secondary location.

## Status

This file is a planning/control document. It does not declare any gate GREEN by itself.

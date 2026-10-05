# Wolfe V49 — Unified Zero-Gap Findings Ledger
Date: 2026-10-05
Baseline: V49
Branch: audit/g3-agency-security-tests
Purpose: Single authoritative consolidation of material findings from the 20-agent audit, prior deep audits, frontend↔backend parity reviews, G3 security work, and current follow-up review.

## Status policy
- OPEN = unresolved or fix not safely applied.
- SOURCE-FIXED = code/config change exists; required runtime/retest evidence is still missing.
- RETEST-PASS = original failure reproduced/fixed and required regression evidence passed.
- EVIDENCE-GATE = implementation may be hardened, but certification requires runtime/production evidence.
- DEFERRED = deliberate product/dependency decision still required.
- SUPERSEDED = replaced by a newer canonical finding.
No finding is GREEN from source inspection alone.

## Authoritative material findings

| ID | Stage | Sev | Finding | Current status | Next proof/action |
|---|---|---:|---|---|---|
| WG20-P1-001 | G11/G12/G13 | P1 | Release cannot be certified from static evidence alone | OPEN/BLOCKING | Full runtime, E2E, deployment, backup/restore and Reality Checker evidence |
| WG20-P1-014 | G9 | P1 | Historical Flyway V14 migration gap has no original SQL/history proof | OPEN/BLOCKING | Establish production Flyway schema-history compatibility; do not invent V14 |
| WG20-P1-015 | G0-G13 | P1 | Zero-Gap orchestrator is a validator, not a real 20-agent executor | OPEN/BLOCKING | Implement real provider adapter/orchestration, structured findings and independent Reality Checker |
| WG20-P2-002 | G3/G11 | P2 | HTTP role-matrix test mixes URL matcher and method-level authorization | OPEN | Split route-matcher and method-permission contracts |
| WG20-P2-003 | G10 | P2 | React Router 6.x lifecycle/security exposure | OPEN/DECISION | Migrate to supported patched major with routing regression tests, or formally document accepted residual risk |
| WG20-P2-004 | G8 | P2 | Admin response DTO boundary inconsistent | PARTIALLY SOURCE-FIXED | AdminController DTO work applied; remaining Admin/Retailer entity responses need exact inventory + serialization tests |
| WG20-P2-005 | G9/G11 | P2 | Docker/CI integration-test boundary mismatch | SOURCE-HARDENED / EVIDENCE-GATE | Clean post-change CI/Docker verification |
| WG20-P2-006 | G9 | P2 | Concurrency evidence noisy; duplicate-key outcomes need invariant classification | OPEN | Deterministic race tests must assert expected winner/rollback/final DB invariant |
| WG20-P2-010 | G10 | P2 | Maven bootstrap/download integrity is not independently pinned | OPEN | Standard wrapper or verified official Maven distribution checksum |
| WG20-P2-011 | G7 | P2 | Email delivery failures were previously swallowed | SOURCE-FIXED | Runtime delivery-failure retest; verify generic external response + server logging |
| WG20-P2-016 | G8 | P2 | Admin GET audit logging gap | SOURCE-FIXED | Runtime audit-event verification |
| WG20-P2-018 | G10/G12 | P2 | Deployment correctness depends on runtime environment/profile evidence | OPEN/EVIDENCE-GATE | Production-like environment validation |
| WG20-P2-024 | G7 | P2 | Password-reset backend capability missing from frontend | SOURCE-FIXED | Browser journey + API retest |
| WG20-P2-025 | G7/G2 | P2 | Review edit/delete/pagination backend capability missing from frontend | SOURCE-FIXED | Browser/API contract retest |
| WG20-P2-026 | G8/G2 | P2 | Inventory reconciliation backend capability missing from frontend | SOURCE-FIXED | Browser/API retest |
| WG20-P2-027 | G8/G2 | P2 | Staff alerts backend capability missing from frontend | SOURCE-FIXED | Browser/API retest |
| WG20-P2-028 | G2/G4/G7 | P2 | Cart/wishlist/bundle mutation failures could be silently swallowed by UI | PARTIALLY SOURCE-FIXED | Re-audit every mutation path and browser failure-state behavior |
| WG20-P2-029 | G7 | P2 | Email-verification prerequisite vs checkout UI journey mismatch | OPEN | Align prerequisite UX/API behavior and verify verified/unverified flows |
| WG20-P2-030 | G7 | P2 | Email verification confirmation frontend missing | SOURCE-FIXED | Browser/API retest |
| WG20-P2-031 | G7 | P3 | Delete-account backend capability lacked frontend journey | SOURCE-FIXED | Browser/API retest + data-lifecycle evidence |
| G3-P2-001 | G3 | P2 | Critical authentication/session regression coverage initially missing/not discoverable | SUPERSEDED | Canonical G3 tests now exist; remaining HTTP/runtime proof tracked by WG20-P2-002 and G3 evidence gates |
| G4-G6-CONCURRENCY-01 | G5/G9 | P2 | Retailer allocation/reservation concurrency requires runtime proof | OPEN/EVIDENCE-GATE | Concurrent allocation integration test and final stock invariant |
| ORDER-LOCK-01 | G5/G9 | P2 | Potential order-create vs cancellation lock-order inversion/deadlock | CONFIRMED/OPEN | Locate exact current services; canonicalize lock ordering and add deterministic deadlock regression test |
| ORDER-DISCOUNT-01 | G6 | P2 | Duplicate identical order lines may collide on discount merge/key identity | OPEN | Verify exact key construction and add duplicate-line pricing/discount regression |
| COOKIE-PUBLIC-01 | G3 | P1 history / closed source | Invalid/stale access cookie could 401 public routes | SOURCE-FIXED | Runtime public/protected endpoint proof |
| JWT-ERROR-01 | G3 | P1 history / closed source | Malformed/signature JWT could become HTTP 500 | SOURCE-FIXED | HTTP negative-path proof |
| RETAILER-ID-01 | G3 | P1 history / closed source | Retailer lookup fell back to mutable email identity | SOURCE-FIXED | Authorization/account-isolation test |
| RETAILER-ROLE-01 | G3 | P1 history / closed source | SecurityConfig/controller retailer role mismatch | SOURCE-ALIGNED | Role matrix runtime proof |
| REFRESH-REUSE-01 | G3 | P1 history / closed source | Refresh-token reuse revocation could roll back transaction | SOURCE-FIXED | Concurrent reuse integration proof |
| RATE-IP-01 | G3/G10 | P2 history / closed source | Proxy/IP rate-limit identity could bucket clients incorrectly | SOURCE-HARDENED | Real Nginx/proxy runtime proof |
| COOKIE-SAMESITE-01 | G3/G10 | P2 history / closed source | SameSite refresh-cookie topology risk | SOURCE-IMPROVED | Real deployment/browser topology proof |
| ADMIN-DTO-01 | G8 | P2 | Raw privileged entities may expose future fields/relationships | PARTIALLY SOURCE-FIXED | Complete AdminController/AdminRetailerController inventory + serialization tests |
| ADMIN-GET-AUDIT-01 | G8 | P2 | GET admin operations were not consistently audited | SOURCE-FIXED | Runtime audit event proof |
| CORRELATION-ID-01 | G10 | P3 | Client-spoofable correlation ID | SOURCE-FIXED | Runtime response/log correlation proof |
| EMAIL-FAIL-01 | G7 | P2 | Registration/verification/reset email failures could leak or fail ambiguously | SOURCE-FIXED | Runtime failure semantics proof |

## Cross-layer parity fixes already applied

| ID | Capability | Status |
|---|---|---|
| PARITY-024 | Password reset UI/API | SOURCE-FIXED |
| PARITY-025 | Review pagination/edit/delete | SOURCE-FIXED |
| PARITY-026 | Inventory reconciliation | SOURCE-FIXED |
| PARITY-027 | Staff alerts | SOURCE-FIXED |
| PARITY-030 | Email verification confirmation | SOURCE-FIXED |
| PARITY-031 | Delete account | SOURCE-FIXED |

## Historical findings not to re-open as new bugs unless new evidence appears

The earlier 100/101-finding ledgers contained many stale, duplicate, design-only, or already-fixed claims. They remain historical evidence, not active findings. Examples include claims contradicted by current source around coupon locking, customer locking, email regex, idempotency header handling, and several previously remediated authorization/security controls.

## Current severity picture

This ledger deliberately does NOT pretend that all historical findings are simultaneously open.

- Confirmed/active P1 blockers: WG20-P1-001, WG20-P1-014, WG20-P1-015.
- Active P2/P3 work: see table above.
- Many security findings are source-fixed but still require runtime evidence.
- No confirmed static P0 currently established.
- Final certification remains BLOCKED until G11/G12/G13 evidence gates pass.

## Required final sequence

1. Finish safe source fixes for genuinely OPEN findings.
2. Consolidate/normalize regression tests.
3. Run one complete verification cycle in healthy PostgreSQL/Redis/Docker environment.
4. Collect API/browser/deployment/backup/restore evidence.
5. Independent Reality Checker retest.
6. Update every finding individually to RETEST-PASS or RETEST-FAIL.
7. Only then issue G13 certification.

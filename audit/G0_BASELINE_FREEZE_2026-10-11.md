# Wolfe G0 Baseline Freeze — 2026-10-11

## Audit identity

- Repository: `aichohtan-dev/wolfe`
- Branch audited: `main`
- Frozen commit: `86208f6edf5bbb7104dc26bdf806c01970818692`
- Commit URL: https://github.com/aichohtan-dev/wolfe/commit/86208f6edf5bbb7104dc26bdf806c01970818692
- Audit work branch: `audit/g0-baseline-2026-10-11`
- Scope: G0 baseline/evidence control, with G1 architecture/security source rechecks.
- Rule: no source/static finding is marked runtime-verified unless the exact run/command is evidenced. No changes are made directly to `main`.

## Audit status policy — preserve prior closures

**A fresh audit does not reopen a finding that was already fixed and verified.** Historical green/closed findings remain closed unless current source or test evidence shows a specific regression. Re-checking code updates evidence; it does not change a status by itself.

Use these separate statuses:
- **FIXED + VERIFIED** — fix exists and the named regression/CI evidence passes.
- **FIXED — SOURCE / RETEST PENDING** — source mitigation exists, but the exact regression test is not yet evidenced.
- **OPEN — CONFIRMED** — a reproducible defect is demonstrated in current source/runtime.
- **EVIDENCE-GATE / NOT VERIFIED** — required proof is missing; this is not a confirmed code defect.
- **PARTIAL** — some concrete coverage exists, with a clearly scoped remainder.
- **HISTORICALLY CLOSED — PRESERVED** — prior closure retained; no confirmed regression found in this pass.

The rows below are a reconciliation of release/certification evidence, not a wholesale reopening of previously fixed code findings. Where older ledgers used OPEN without current reproduction, this report reclassifies it as an evidence gap until a concrete defect is demonstrated.

## Baseline evidence

| Evidence | Result | Link |
|---|---|---|
| Frozen-main Wolfe CI | PASS at frozen SHA; frontend typecheck/lint/build/contract/security steps passed; backend Maven tests passed against CI services; dependency/secret checks passed | https://github.com/aichohtan-dev/wolfe/actions/runs/38144338357 |
| Frozen-main Docker Verification | PASS at frozen SHA; Compose validation/build/start, PostgreSQL/API health, DB integrity preflight, encrypted backup and isolated PostgreSQL restore, endpoint/reverse-proxy checks passed | https://github.com/aichohtan-dev/wolfe/actions/runs/38144338442 |
| Audit-branch Wolfe CI | PASS for audit report commit `5475e932ca8187cf6b78419caa6282e578b5027f`; frontend, backend, contract and security jobs passed | https://github.com/aichohtan-dev/wolfe/actions/runs/38145000817 |
| Audit-branch Docker Verification | PASS for audit report commit `5475e932ca8187cf6b78419caa6282e578b5027f`; stack health, DB preflight, encrypted backup/restore and reverse-proxy checks passed | https://github.com/aichohtan-dev/wolfe/actions/runs/38145000787 |
| Flyway source inventory | Migrations V1–V13 and V15–V62 exist; V14 is intentionally absent and V56 documents the gap | https://github.com/aichohtan-dev/wolfe/blob/86208f6edf5bbb7104dc26bdf806c01970818692/docs/MIGRATION_HISTORY.md |
| Orchestrator | `agents.yaml` lists 20 agent identities, but `run.py` is a fail-closed validator and explicitly says the provider execution adapter is not enabled | https://github.com/aichohtan-dev/wolfe/blob/86208f6edf5bbb7104dc26bdf806c01970818692/audit/orchestrator/run.py |
| Reporting policy | Runtime PASS requires execution of the exact artifact and recorded evidence | https://github.com/aichohtan-dev/wolfe/blob/86208f6edf5bbb7104dc26bdf806c01970818692/docs/REPORTING_POLICY.md |

CI evidence does not certify real production deployment, browser E2E coverage, production database migration history, or independent G13 acceptance.

## G0 outcome

**G0 BASELINE FROZEN — PASS.** The current main SHA is recorded, current workflow evidence is linked, and follow-up audit work is isolated on a dedicated branch. Overall Zero-Gap certification remains **NOT COMPLETE**.

## Canonical carry-forward register — status reconciled, not blindly reopened

| ID | Severity / Stage | Reconciled status | Evidence and exact next step |
|---|---|---|---|
| WG20-P1-001 | P1 / G11–G13 | EVIDENCE-GATE / certification blocker | Docker CI proves CI encrypted backup and isolated PostgreSQL restore on this SHA. Browser E2E, production-like deployment, production observability, and independent G13 acceptance are still not evidenced. This is a release evidence gate, not proof of a current source defect. |
| WG20-P1-014 | P1 / G9–G12 | EVIDENCE-GATE | V14 is absent and docs say the gap is intentional; this alone neither proves nor disproves compatibility with the actual production Flyway history. Obtain trustworthy production `flyway_schema_history`/deployment evidence and compare applied versions/checksums. Do not invent V14. |
| WG20-P1-015 | P1 / G0–G13 | OPEN — CONFIRMED capability gap | The manifest lists 20 roles, but `audit/orchestrator/run.py` does not call a provider, persist structured findings, or run an independent Reality Checker. Implement and test provider-backed execution before claiming 20-agent execution. |
| WG20-P2-002 | P2 / G3 | FIXED — URL matcher source/test; broader coverage pending | Current `SecurityConfig` retailer matcher aligns with `ROLE_RETAILER`, and `G3RoleMatrixHttpSecurityTest` exists; backend CI passed. Do not leave the old generic URL-matcher issue marked OPEN. Separately verify method-level authorization on mapped controller endpoints with negative HTTP tests. |
| WG20-P2-004 / ADMIN-DTO-01 | P2 / G8 | PARTIAL — scoped coverage gap | These IDs may describe the same admin DTO/serialization root cause and must be deduplicated. Review each privileged admin response DTO and add serialization assertions only where a concrete untested boundary remains; do not count these as two bugs without distinct reproduction. |
| WG20-P2-006 | P2 / G5–G9 | EVIDENCE-GATE / runtime coverage pending | Existing source-contract and concurrency tests are evidence and should remain credited. What remains is deterministic competing-transaction outcome proof for the specific business invariants; do not call this a confirmed defect without a failing invariant/reproduction. |
| WG20-P2-010 | P2 / G10 | EVIDENCE-GATE | Verify Maven wrapper/bootstrap and downloaded build-input integrity independently. Successful Maven tests are positive build evidence but do not alone prove bootstrap integrity; no concrete exploit/defect is asserted here. |
| WG20-P2-018 | P2 / G12 | EVIDENCE-GATE | Docker CI stack/health/backup/restore passed. Production-like deployment and production configuration/observability evidence remain outstanding. |
| WG20-P2-028 | P2 / G4/G7 | PARTIAL / verification pending | Trace cart/wishlist/bundle mutation failures through frontend and API. Add tests only for concrete uncovered paths where errors are hidden or state is lost; absence of a trace is not itself a confirmed bug. |
| WG20-P2-029 | P2 / G5/G7 | EVIDENCE-GATE / targeted tests pending | Verify checkout behavior when email verification is missing and compare UI/API responses. Keep as a test gap until current source plus a failing case confirms a defect. |
| G4-G6-CONCURRENCY-01 | P2 / G4–G6 | EVIDENCE-GATE | Need deterministic runtime competing-transaction tests for retailer allocation/reservation and proof of stock/settlement invariants. This is not a confirmed bug by itself. |
| ORDER-LOCK-01 | P2 / G5/G9 | PARTIAL / runtime evidence pending | A source-contract test checks variant-before-inventory locking and deterministic TreeMap ordering. Credit that test; the separate create-versus-cancel deadlock scenario is not yet shown as a deterministic runtime regression test. |
| ORDER-DISCOUNT-01 | P2 / G5/G6 | FIXED — SOURCE / regression proof added, CI pending | Source merges identical product/bundle/configuration/variant lines before discount allocation. Added `duplicateIdenticalLinesMergeBeforeCouponDiscountAllocationAndPersistence` in `backend/src/test/java/com/wolfe/order/OrderServiceTest.java` on the audit branch. It asserts merged quantity, single persisted order line, coupon discount allocation, line net amount, order subtotal/discount/shipping/total, and stock reservation. Mark FIXED + VERIFIED only after the new commit's backend CI passes. |
| G0-DOC-001 | P2 / G0 | OPEN — CONFIRMED documentation inconsistency | README has a “Next modules” list naming cart/accounts/wishlist/checkout/orders/admin as future work despite these modules existing, plus older V18.7/local dependency notes that conflict with current CI evidence. Rewrite to one current status, distinguish intentional payment deferral from implemented modules, and mark historic notes archival. |

The carry-forward rows are not necessarily unique root causes. Deduplicate historical IDs before counting. Do not claim all ledger entries are bugs, and do not convert missing evidence into a source defect.

## G1 source checks — batch 1

- **JWT filter / public-route behavior — FIXED + CI-VERIFIED for tested cases:** source catches token parsing/authentication failures, clears the security context, and continues the chain so authorization rules decide whether a route can be anonymous. CI security tests passed. Keep broader mapped-endpoint negative coverage as a scoped test gap, not a reopening of the fixed cookie-handling bug.
- **Refresh-token reuse — FIXED + backend-CI-VERIFIED for existing tests:** source uses `@Transactional(noRollbackFor = InvalidRefreshTokenReuseException.class)`, locks the customer, revokes the token family, increments session version, and saves the customer before raising the dedicated exception. Security tests are present and backend CI passed. A deterministic multi-session race test is additional assurance, not grounds to reopen the fix without a regression.
- **Retailer account binding — FIXED + SOURCE-VERIFIED:** source binds retailer lookup to `findByUserId(customerId)`; the inspected method has no email fallback.
- **Retailer authorization matcher — FIXED + SOURCE/CI-VERIFIED:** `SecurityConfig` grants `/api/v1/retailer/**` only to RETAILER, matching the controller's role check. The URL-level role matrix test exists and passed in backend CI.
- **Order discount merge — FIXED — SOURCE / RETEST PENDING:** duplicate equivalent lines are aggregated in `mergedLines` before discount allocation. Exact duplicate-line/persisted-total regression proof remains pending.
- **Lock ordering — SOURCE-CONTRACT TEST PRESENT:** existing checks are useful and remain credited; they are not the same as a deterministic concurrent create/cancel runtime test.
- **Historical documentation — ARCHIVAL LABEL NEEDED:** `WOLFE_G5_G13_FINAL_GATE_STATUS.md` and README contain V49/V18.7-era runtime limitation statements. Mark historical snapshots as archival/superseded rather than carrying their old runtime status forward as current.

## G1 source checks — batch 2 (Nginx, reverse proxy, HTTP security)

- **TLS and HTTP-to-HTTPS — SOURCE PASS:** `nginx.conf.template` redirects port 80 to HTTPS and configures TLS 1.2/1.3. Docker Verification passed its generated-TLS endpoint/reverse-proxy checks on the audit report commit. This is CI-stack evidence, not production certificate/domain validation.
- **Security headers — SOURCE PASS, browser-policy validation pending:** Nginx sets HSTS, nosniff, frame denial, Referrer-Policy, Permissions-Policy, CSP, COOP and CORP. Spring Security also sets CSP/frame/referrer/cross-origin headers. No confirmed defect from duplication alone; verify effective response headers in deployed browser flows before release.
- **Forwarded client address — SOURCE PASS for direct Nginx-to-API topology:** `ops/proxy_params` overwrites `X-Real-IP` and `X-Forwarded-For` with Nginx's `$remote_addr`, preventing a caller-supplied forwarded-for value from being blindly passed through. If another trusted proxy/CDN is placed in front, its trust boundary and real-IP configuration need separate review.
- **PDF import upload limit — SOURCE REVIEW PASS / rate coverage scoped:** exact controller mapping is only `POST /api/v1/admin/pdf-imports/upload`; controller class requires ADMIN/SUPER_ADMIN. That exact Nginx route has a dedicated 5r/m rate zone with burst 2 and a 50 MB body cap. Other mappings are job/media/item management, not alternate uploads. No alternate upload path was found, so no rate-limit bypass is confirmed.
- **Global API limit — SOURCE PASS:** other `/api/` paths receive the global API limit and request body/time limits. The PDF import prefix is handled earlier, so it does not inherit the global zone.
- **HTTP authorization — SOURCE PASS for URL rules:** `SecurityConfig` requires ADMIN/SUPER_ADMIN for `/api/v1/admin/**` and RETAILER for `/api/v1/retailer/**`; CSRF is enabled, CORS origins come from configuration, and the session policy is stateless. Mapped method-level permission checks exist in key controllers; systematic negative HTTP tests across the complete map remain pending.
- **New confirmed security defects from this batch:** none. Remaining items above are mapped-endpoint/browser/production evidence gates, not confirmed vulnerabilities.

## G1 source checks — batch 3 (order discount regression)

- **Source code changed on audit branch:** `backend/src/test/java/com/wolfe/order/OrderServiceTest.java` adds a focused regression test for duplicate identical order lines. Production logic is intentionally unchanged because the merge mitigation already exists.
- **Expected invariant:** three identical units become one persisted line of quantity 3; coupon discount 5,000 paise is applied once; line net 25,000 paise; subtotal 30,000; discount 5,000; standard shipping 19,900; total 44,900; inventory reservation 3.
- **Verification state:** source/test file is committed, but CI has not yet reported for the new commit. This is NOT a test PASS until GitHub Actions completes successfully. The new test is a Mockito unit test, not a PostgreSQL concurrency/runtime proof.

## Next steps (G1 → G2)

1. Run the new duplicate-identical-order-lines regression test through backend CI; reconcile any mock/signature/test failures before marking verified.
2. Inspect mapped admin/retailer endpoints for method-level authorization and DTO leakage, recording exact endpoints and tests; don't reopen already verified URL-role fixes.
3. Add/confirm negative HTTP tests for invalid/expired JWTs on representative public and protected mapped endpoints.
4. Verify checkout email-verification behavior from current source and targeted positive/negative tests.
5. Add deterministic PostgreSQL concurrency tests for create/cancel lock ordering and retailer allocation; retain existing passing source-contract tests as evidence.
6. Collect production Flyway history evidence without changing migration history based on assumptions.
7. Keep G11 browser E2E, G12 production-like deployment/observability, and G13 independent acceptance as explicit release evidence gates.
8. Reconcile the README and label older audit snapshots as archival; preserve previously fixed/verified status unless a specific regression is reproduced.

## Certification statement

This file records a **G0 baseline freeze and initial G1 source rechecks**, not full G1 completion and not release certification. Both workflows passed for frozen main, and both workflows also passed for the prior audit-report commit listed above. A new regression-test commit has now been added and is awaiting its own CI evidence. Historical closures are preserved; outstanding items are scoped test/evidence gates or confirmed capability/documentation gaps, not an instruction to reopen every old finding.

# Wolfe G0 Baseline Freeze — 2026-10-11

## Audit identity

- Repository: `aichohtan-dev/wolfe`
- Branch audited: `main`
- Frozen commit: `86208f6edf5bbb7104dc26bdf806c01970818692`
- Commit URL: https://github.com/aichohtan-dev/wolfe/commit/86208f6edf5bbb7104dc26bdf806c01970818692
- Audit work branch: `audit/g0-baseline-2026-10-11`
- Scope: G0 baseline/evidence control, with initial G1 architecture/security source recheck.
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
| Wolfe CI | PASS at frozen SHA; frontend typecheck/lint/build/contract/security steps passed; backend Maven tests passed against CI services; dependency/secret checks passed | https://github.com/aichohtan-dev/wolfe/actions/runs/38144338357 |
| Docker Verification | PASS at frozen SHA; Compose validation/build/start, PostgreSQL/API health, DB integrity preflight, encrypted backup and isolated PostgreSQL restore, endpoint/reverse-proxy checks passed | https://github.com/aichohtan-dev/wolfe/actions/runs/38144338442 |
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
| ORDER-DISCOUNT-01 | P2 / G5/G6 | FIXED — SOURCE / regression proof pending | `OrderService.create` merges equivalent lines before discount allocation, mitigating the reported duplicate-key collision. Add a regression test submitting duplicate identical lines and asserting merged quantity, discount allocation, and persisted totals; then run it before marking RETEST-PASS. Do not label this OPEN without evidence the source fix regressed. |
| G0-DOC-001 | P2 / G0 | OPEN — CONFIRMED documentation inconsistency | README has a “Next modules” list naming cart/accounts/wishlist/checkout/orders/admin as future work despite these modules existing, plus older V18.7/local dependency notes that conflict with current CI evidence. Rewrite to one current status, distinguish intentional payment deferral from implemented modules, and mark historic notes archival. |

The carry-forward rows are not necessarily unique root causes. Deduplicate historical IDs before counting. Do not claim all ledger entries are bugs, and do not convert missing evidence into a source defect.

## G1 preliminary source checks

- **JWT filter / public-route behavior — FIXED + CI-VERIFIED for tested cases:** source catches token parsing/authentication failures, clears the security context, and continues the chain so authorization rules decide whether a route can be anonymous. CI security tests passed. Keep broader mapped-endpoint negative coverage as a scoped test gap, not a reopening of the fixed cookie-handling bug.
- **Refresh-token reuse — FIXED + backend-CI-VERIFIED for existing tests:** source uses `@Transactional(noRollbackFor = InvalidRefreshTokenReuseException.class)`, locks the customer, revokes the token family, increments session version, and saves the customer before raising the dedicated exception. Security tests are present and backend CI passed. A deterministic multi-session race test is additional assurance, not grounds to reopen the fix without a regression.
- **Retailer account binding — FIXED + SOURCE-VERIFIED:** source binds retailer lookup to `findByUserId(customerId)`; the inspected method has no email fallback.
- **Retailer authorization matcher — FIXED + SOURCE/CI-VERIFIED:** `SecurityConfig` grants `/api/v1/retailer/**` only to RETAILER, matching the controller's role check. The URL-level role matrix test exists and passed in backend CI.
- **Order discount merge — FIXED — SOURCE / RETEST PENDING:** duplicate equivalent lines are aggregated in `mergedLines` before discount allocation. Exact duplicate-line/persisted-total regression proof remains pending.
- **Lock ordering — SOURCE-CONTRACT TEST PRESENT:** existing checks are useful and remain credited; they are not the same as a deterministic concurrent create/cancel runtime test.
- **Historical documentation — ARCHIVAL LABEL NEEDED:** `WOLFE_G5_G13_FINAL_GATE_STATUS.md` and README contain V49/V18.7-era runtime limitation statements. Mark historical snapshots as archival/superseded rather than carrying their old runtime status forward as current.

## Next steps (G1 → G2)

1. Add and run the duplicate-identical-order-lines regression test; assert merged quantity, discount allocation, and persisted totals.
2. Inspect mapped admin/retailer endpoints for method-level authorization and DTO leakage, recording exact endpoints and tests; don't reopen already verified URL-role fixes.
3. Add/confirm negative HTTP tests for invalid/expired JWTs on representative public and protected mapped endpoints.
4. Verify checkout email-verification behavior from current source and targeted positive/negative tests.
5. Add deterministic PostgreSQL concurrency tests for create/cancel lock ordering and retailer allocation; retain existing passing source-contract tests as evidence.
6. Collect production Flyway history evidence without changing migration history based on assumptions.
7. Keep G11 browser E2E, G12 production-like deployment/observability, and G13 independent acceptance as explicit release evidence gates.
8. Reconcile the README and label older audit snapshots as archival; preserve previously fixed/verified status unless a specific regression is reproduced.

## Certification statement

This file records a **G0 baseline freeze and initial G1 source recheck**, not full G1 completion and not release certification. The current commit's two successful CI workflows are linked above. Historical closures are preserved; outstanding items are scoped test/evidence gates or confirmed gaps, not an instruction to reopen every old finding.

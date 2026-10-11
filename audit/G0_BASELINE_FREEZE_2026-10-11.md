# Wolfe G0 Baseline Freeze — 2026-10-11

## Audit identity

- Repository: `aichohtan-dev/wolfe`
- Branch audited: `main`
- Frozen commit: `86208f6edf5bbb7104dc26bdf806c01970818692`
- Commit URL: https://github.com/aichohtan-dev/wolfe/commit/86208f6edf5bbb7104dc26bdf806c01970818692
- Audit work branch: `audit/g0-baseline-2026-10-11`
- Scope: G0 baseline/evidence control, with initial G1 architecture/security source recheck.
- Rule: no source/static finding is marked runtime-verified unless the exact run/command is evidenced. No changes are made directly to `main`.

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

## Initial canonical findings — rechecked against frozen SHA

Status vocabulary: OPEN, PARTIAL, FIXED — SOURCE, RETEST-PASS, EVIDENCE-GATE, BLOCKING. The entries below are not closed solely by historic green marks.

| ID | Severity / Stage | Initial status | Current evidence and next closure step |
|---|---|---|---|
| WG20-P1-001 | P1 / G11–G13 | BLOCKING / EVIDENCE-GATE | Docker CI now proves a CI encrypted backup and isolated PostgreSQL restore on this SHA. Still missing browser E2E evidence, production-like deployment proof, production observability evidence, and independent Reality Checker acceptance. |
| WG20-P1-014 | P1 / G9–G12 | BLOCKING / EVIDENCE-GATE | V14 is absent and docs say the gap is intentional; this does not prove compatibility with the actual production Flyway schema history. Obtain trustworthy production `flyway_schema_history`/deployment evidence and compare checksums/applied versions. Do not invent V14. |
| WG20-P1-015 | P1 / G0–G13 | BLOCKING | The agent manifest lists 20 roles, but `audit/orchestrator/run.py` does not call a provider, persist findings, or run the independent agent. Implement and test the provider-backed pipeline and structured result persistence before claiming 20-agent execution. |
| WG20-P2-002 | P2 / G3 | OPEN | `G3RoleMatrixHttpSecurityTest` exercises URL matcher outcomes using intentionally unmapped paths. Add/assert controller method-level authorization and complete mapped endpoint role matrix. |
| WG20-P2-004 | P2 / G8 | PARTIAL | Continue endpoint-by-endpoint review of admin DTO boundaries and serialization tests for privileged fields; a generic or historical green label is insufficient. |
| WG20-P2-006 | P2 / G5–G9 | OPEN | Classify concurrent outcomes against business invariants; capture deterministic competing-transaction runtime evidence. |
| WG20-P2-010 | P2 / G10 | OPEN | Verify Maven wrapper/bootstrap and downloaded build-input integrity independently; successful Maven tests alone do not close supply-chain integrity. |
| WG20-P2-018 | P2 / G12 | EVIDENCE-GATE | Docker CI stack/health/backup restore passed, but a production-like deployment and production configuration/observability behavior still need evidence. |
| WG20-P2-028 | P2 / G4/G7 | PARTIAL | Trace cart/wishlist/bundle mutation failures through frontend and API; add tests that errors are surfaced and state is not silently lost. |
| WG20-P2-029 | P2 / G5/G7 | OPEN | Add positive/negative checkout tests for email verification prerequisite and align UI/API error behavior. |
| G4-G6-CONCURRENCY-01 | P2 / G4–G6 | EVIDENCE-GATE | Need runtime competing-transaction tests for retailer allocation/reservation and proof of stock/settlement invariants. |
| ORDER-LOCK-01 | P2 / G5/G9 | PARTIAL / EVIDENCE-GATE | A source-contract test checks variant-before-inventory locking and deterministic TreeMap ordering. It is not a deterministic concurrent create-versus-cancel deadlock regression test; add and run one. |
| ORDER-DISCOUNT-01 | P2 / G5/G6 | FIXED — SOURCE; regression proof open | `OrderService.create` merges equivalent lines before discount allocation, mitigating the reported duplicate-key collision. Add a regression test that submits duplicate identical lines and asserts merged quantity, discount allocation, and persisted totals; only then mark RETEST-PASS. |
| ADMIN-DTO-01 | P2 / G8 | PARTIAL | Verify every admin API response uses explicit DTOs; add serialization assertions against entity relationships and privileged-field leakage. |
| G0-DOC-001 | P2 / G0 | CONFIRMED — SOURCE | README contains contradictory historical/current instructions: a “Next modules” list names cart/accounts/wishlist/checkout/orders/admin as future work, despite these modules existing, and older V18.7/local dependency-limitation notes conflict with current CI evidence. Rewrite into one current status, separate intentional payment deferral from implemented modules, and label historical notes as archival. |

The 14 carry-forward rows from the earlier reconciliation are not necessarily 14 distinct root causes. `G0-DOC-001` is an additional documentation finding. Do not sum old ledger counts until all historical IDs are deduplicated.

## G1 preliminary source checks

- **JWT filter / public-route behavior:** source catches token parsing/authentication failures, clears the security context, then continues the filter chain so authorization rules decide whether a route can be anonymous. This is positive source evidence; current CI passed the security test job. Keep any broader endpoint matrix finding open until mapped-route negative tests are demonstrated.
- **Refresh-token reuse:** source uses `@Transactional(noRollbackFor = InvalidRefreshTokenReuseException.class)`, locks the customer, revokes the token family, increments session version, and saves the customer before raising the dedicated exception. Unit security tests are present and the backend test job passed. Production multi-session race behavior still needs a deterministic integration scenario if the gate requires it.
- **Retailer account binding:** source now binds retailer lookup to `findByUserId(customerId)` and requires `ROLE_RETAILER`; no email fallback is present in the inspected method.
- **Retailer authorization matcher:** `SecurityConfig` grants `/api/v1/retailer/**` only to RETAILER, matching the controller's role check. The URL-level role matrix test exists and passed in the backend test suite.
- **Order discount merge:** duplicate equivalent lines are aggregated in `mergedLines` before discount allocation. Regression proof is missing from the inspected order-test directory, which currently contains `OrderServiceTest`, `OrderServiceValidationTest`, and `OrderLockOrderContractTest`.
- **Lock ordering:** source-contract checks exist; do not mislabel them as full concurrent runtime coverage.
- **Historical documentation:** `WOLFE_G5_G13_FINAL_GATE_STATUS.md` and README contain V49/V18.7-era runtime limitation statements. They should be clearly marked archival or superseded by current commit-specific evidence.

## Next steps (G1 → G2)

1. Add a regression test for duplicate identical order lines and persisted discount/total invariants.
2. Inspect all admin and retailer mapped endpoints for method-level authorization and DTO leakage.
3. Verify the full invalid/expired JWT behavior on representative public and protected mapped endpoints.
4. Review checkout email-verification UI/API negative behavior.
5. Add deterministic PostgreSQL concurrency tests for create/cancel lock ordering and retailer allocation.
6. Collect production Flyway history evidence without changing migration history based on assumptions.
7. Keep G11 browser E2E, G12 production-like deployment/observability, and G13 independent acceptance as explicit release gates.

## Certification statement

This file records a **G0 baseline freeze and initial source recheck**, not full G1 completion and not release certification. The current commit's two successful CI workflows are linked above; outstanding rows remain open until their exact closure evidence is attached.

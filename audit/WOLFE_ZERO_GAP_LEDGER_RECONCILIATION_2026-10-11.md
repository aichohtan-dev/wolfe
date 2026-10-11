# Wolfe Zero-Gap Ledger Reconciliation — 2026-10-11

**Baseline:** `main` at `57b3d4e4c7c879ba6ca4843a8e43d3308ee6b8f4`  
**Purpose:** Reconcile conflicting historical closure claims against the active V49 unified ledger without declaring unverified work complete.  
**Scope:** Documentation/evidence control only. This report does not itself certify runtime or production readiness.

## Executive result

The repository has two incompatible status sources:

1. [Historical 001–100 master ledger](../WOLFE_ZERO_GAP_FINDINGS_001_100_MASTER.md) reports 100/100 GREEN, while its own caveat says runtime certification is separate and was not established by static inspection alone.
2. [V49 unified ledger](Wolfe_V49_Unified_Zero_Gap_Findings_Ledger_2026-10-05.md) retains active P1 blockers and open/partial P2 findings.

**Decision: Zero-Gap certification is NOT COMPLETE.** The historical 100/100 count is a legacy source-review status, not an independently evidenced release certification. Until the two sources are reconciled row-by-row, do not advertise “100/100 certified.”

## Canonical status rules

Use these meanings consistently in future updates:

- **FIXED — SOURCE:** Code change is visible, but test/runtime evidence is not yet sufficient to close the finding.
- **RETEST-PASS:** A named reproducible test passed against the current commit and the evidence is linked.
- **OPEN:** The defect or required proof is outstanding.
- **PARTIAL:** A source change exists, but a defined portion remains unresolved.
- **EVIDENCE-GATE:** The claim requires environment/runtime/deployment proof; source inspection alone cannot close it.
- **BLOCKING:** A release/certification gate that remains unresolved.

A green mark in a historical list is not enough to satisfy RETEST-PASS. Every canonical finding must have one stable ID, one current status, an owner, a reproducible verification command/scenario, evidence URL or run ID, and the commit SHA tested.

## Active blockers carried forward

| ID | Current status | Required closure evidence |
|---|---|---|
| WG20-P1-001 | BLOCKING / EVIDENCE-GATE | Reproducible runtime + browser E2E + deployment checks; backup/restore and observability evidence; independent Reality Checker acceptance. |
| WG20-P1-014 | BLOCKING / EVIDENCE-GATE | Establish production Flyway schema-history compatibility and original V14 migration history from trustworthy evidence. Do not invent or recreate a historical migration based on assumptions. |
| WG20-P1-015 | BLOCKING | Demonstrate a real 20-agent/provider-backed execution pipeline, structured finding persistence, and independent Reality Checker review. A validator/orchestrator scaffold alone is not proof that 20 independent agents executed. |

## Open/partial P2 and related rows carried forward

| ID | Current status | Reconciliation / next action |
|---|---|---|
| WG20-P2-002 | OPEN | Separate URL matcher and method-level authorization assertions; test the full HTTP role matrix. |
| WG20-P2-004 | PARTIAL | Review admin DTO boundaries and verify serialization tests for privileged fields. |
| WG20-P2-006 | OPEN | Classify concurrency outcomes against invariants; distinguish expected duplicate-key conflicts from real invariant failures. |
| WG20-P2-010 | OPEN | Pin/verify Maven bootstrap and downloaded build inputs with independent integrity evidence. |
| WG20-P2-018 | EVIDENCE-GATE | Verify deployment behavior in a production-like environment; static configuration checks do not close it. |
| WG20-P2-028 | PARTIAL | Trace cart/wishlist/bundle mutation errors through UI and API; prove failures are visible and state is not silently lost. |
| WG20-P2-029 | OPEN | Align email-verification prerequisites and checkout UI/API behavior; add positive and negative tests. |
| G4-G6-CONCURRENCY-01 | EVIDENCE-GATE | Runtime proof for retailer allocation/reservation concurrency under competing transactions. |
| ORDER-LOCK-01 | OPEN | Analyze and test order-create versus cancellation lock ordering; add a deterministic concurrency/deadlock regression test. |
| ORDER-DISCOUNT-01 | SOURCE RECHECK — likely source-fixed; not closed | Current `OrderService.create` merges equivalent lines into `mergedLines` before allocating discounts, and uses the merged line key for `lineDiscounts`. This addresses the previously described duplicate-key collision mechanism at source level. Add a regression test with duplicate identical input lines and verify persisted item quantities and total discount allocation before marking RETEST-PASS. |
| ADMIN-DTO-01 | PARTIAL | Verify every admin API response uses explicit DTOs and serialization tests prevent privileged entity fields/relationships from leaking. |

The 3 P1 rows plus 11 carried-forward P2/related rows equal **14 ledger entries requiring closure or explicit evidence**, not necessarily 14 distinct root causes. IDs may overlap the historical 001–100 ledger; do not add the counts together as a unique finding total without row-by-row deduplication.

## Current source recheck: ORDER-DISCOUNT-01

At the baseline commit above, `backend/src/main/java/com/wolfe/order/OrderService.java`:
- builds a composite key from product ID, bundle ID, configuration token, and variant ID;
- merges duplicate matching lines in `mergedLines`;
- builds the `lines` list from merged map values before discount allocation;
- keys `lineDiscounts` using the merged line key.

This is positive static evidence that the originally described same-key collision path is mitigated. It is **not** a test result. Add a regression test that submits duplicate equivalent order lines, verifies the merged quantity and discount sum, and confirms persisted order totals. Until that test runs on the target commit, keep the finding out of RETEST-PASS.

## Recent merged security changes (context only)

- PR #40: [refresh-token expiry versus reuse](https://github.com/aichohtan-dev/wolfe/pull/40), squash commit `ed33fa27a4f5d4df2c05b1384794244bd648e98f`.
- PR #41: [PDF catalog upload rate limit](https://github.com/aichohtan-dev/wolfe/pull/41), squash commit `57b3d4e4c7c879ba6ca4843a8e43d3308ee6b8f4`.
- Post-merge CI was reported green for backend, frontend, security, and docker-verify. CI green is useful evidence for those workflows, but does not replace runtime/deployment/backup-restore certification.

## Required reconciliation workflow

1. Freeze the baseline SHA for each audit run.
2. Import all rows from both historical ledgers into one canonical table keyed by stable finding ID.
3. Deduplicate by root cause and affected trust boundary; preserve all old IDs as aliases.
4. For each row, record source commit, changed paths, test name/command, CI run, runtime environment, outcome, and Reality Checker decision.
5. Reclassify unsupported historical greens to OPEN or EVIDENCE-GATE; do not delete historical history.
6. Close source-fixed issues only after the required regression test passes; close runtime gates only with reproducible environment evidence.
7. Publish totals split by unique root cause and evidence status: RETEST-PASS, FIXED—SOURCE, PARTIAL, OPEN, EVIDENCE-GATE, BLOCKING.
8. Certification remains blocked until all P1s are closed and independent G13 review accepts the evidence.

## Release statement

**Current defensible statement:** Main CI passed the four reported workflows after PRs #40 and #41. The repository's Zero-Gap audit ledgers remain inconsistent, and runtime/deployment certification has not been demonstrated by this reconciliation. Do not claim Zero-Gap certification yet.

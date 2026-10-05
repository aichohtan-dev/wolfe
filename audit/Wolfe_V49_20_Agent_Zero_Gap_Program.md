# Wolfe V49 — 20-Agent Zero-Gap Audit Program

## Objective
Run an independent, evidence-driven audit of Wolfe V49 through G0-G13. No finding is GREEN from source inspection alone. GREEN requires reproducible evidence, regression verification, and independent Reality Checker acceptance.

## Baseline
- Baseline: V49
- Current audit branch: audit/g3-agency-security-tests
- Roadmap: G0 Baseline & Source Freeze -> G1 Architecture & Trust Boundaries -> G2 API Contract -> G3 Auth/Session/Authorization -> G4 Catalog/Cart/Configurator/Bundle -> G5 Checkout/Orders/Inventory/Fulfillment -> G6 Money/Coupons/Refund/Settlement -> G7 Returns/Customer/Recovery/Notifications -> G8 Admin/Retailer/PDF/Media -> G9 Database/Migration/Concurrency -> G10 Security/Docker/Nginx/CI/CD/Supply Chain -> G11 Runtime/Browser E2E/Negative Testing -> G12 Deployment/Backup/Restore/Observability -> G13 Independent Final Audit & Certification.

## Mandatory Finding Schema
Every finding must contain:
Finding ID; G-stage; Severity P0/P1/P2/P3; Title; affected file/class/method/endpoint/component; observed behavior; expected behavior; exact evidence; reproduction steps; impact; root cause; recommended fix; regression risk; verification test; Status (NEW/CONFIRMED/FIXED/RETEST-PASS/RETEST-FAIL).

## Evidence Rules
1. Compilation is not functional proof.
2. Static source inspection is not runtime proof.
3. A test that only asserts source text is contract evidence, not runtime evidence.
4. Security findings require negative-path testing where practical.
5. Financial/inventory/concurrency findings require transaction or integration evidence where practical.
6. Do not silently close duplicate findings; link them to a canonical finding.
7. Conflicting agent conclusions must be retained and independently resolved.
8. No GREEN certification until Reality Checker accepts the evidence.

## Agent Assignments

### Security Wave
1. Security Architect
Scope: trust boundaries, security architecture, authentication/session model, authorization model, threat model, security controls, cross-module privilege boundaries.
Primary stages: G1,G3,G10,G12.

2. Application Security Engineer
Scope: OWASP-style application security, injection, XSS/CSRF, JWT, cookies, session fixation/reuse, IDOR, SSRF, secrets, authorization bypass.
Primary stages: G2,G3,G6,G7,G8,G10,G11.

3. Penetration Tester
Scope: attacker-path analysis, privilege escalation, IDOR, authentication abuse, replay, race conditions, endpoint abuse, negative testing.
Primary stages: G3,G5,G6,G8,G10,G11.

4. Cloud Security Architect
Scope: deployment trust boundaries, environment separation, secrets, network exposure, TLS, proxy trust, cloud/runtime assumptions, backup exposure.
Primary stages: G1,G10,G12.

5. Compliance Auditor
Scope: privacy/data lifecycle, consent, auditability, retention, access/deletion semantics, security/compliance evidence.
Primary stages: G7,G8,G10,G12.

### Engineering Wave
6. Backend Architect
Scope: Spring architecture, domain boundaries, service/repository correctness, transaction semantics, concurrency, error handling.
Primary stages: G1,G4,G5,G6,G9.

7. Frontend Developer
Scope: React components, state, API integration, auth/session UX, feature parity, error/loading states, frontend security.
Primary stages: G2,G3,G4,G5,G7,G8,G11.

8. Senior Developer
Scope: cross-module defect hunt, code smells with real impact, edge cases, duplicated logic, lifecycle/state inconsistencies.
Primary stages: G1-G10.

9. DevOps Automator
Scope: CI/CD, release automation, workflow correctness, environment configuration, artifact integrity, deployment automation.
Primary stages: G10,G12.

10. Infrastructure Maintainer
Scope: Dockerfiles, compose, Nginx, health checks, service dependencies, ports, volumes, proxy behavior, runtime hardening.
Primary stages: G10,G12.

### Testing/Evidence Wave
11. API Tester
Scope: endpoint inventory, request/response contracts, authentication/authorization matrix, validation, negative cases, status codes.
Primary stages: G2,G3,G4,G5,G6,G7,G8,G11.

12. Test Automation Engineer
Scope: missing unit/integration/security/contract/E2E coverage; deterministic regression suites; concurrency test design.
Primary stages: G3,G5,G6,G9,G11.

13. Performance Benchmarker
Scope: API latency, DB query patterns, N+1/over-fetching, frontend performance, concurrency and bottlenecks.
Primary stages: G4,G5,G6,G9,G11,G12.

14. Test Results Analyzer
Scope: existing test suite quality, false positives, weak assertions, skipped tests, environment gaps, coverage blind spots.
Primary stages: G3,G5,G6,G9,G11.

15. Evidence Collector
Scope: reproducible evidence package for every material finding; commands, logs, screenshots where applicable, test output, commit references.
Primary stages: G0-G13.

16. Accessibility Auditor
Scope: WCAG, keyboard navigation, semantic HTML, labels, contrast, focus, responsive/mobile accessibility, forms and error states.
Primary stages: G2,G4,G7,G8,G11.

### Product/Independent Acceptance Wave
17. Reality Checker
Scope: independently challenge claimed fixes; verify observed behavior against expected behavior; reject unsupported GREEN claims.
Primary stages: G11,G12,G13.

18. Product Manager
Scope: feature completeness, user journeys, business rules, missing capabilities, product-vs-implementation gaps.
Primary stages: G2,G4,G5,G6,G7,G8.

19. Sprint Prioritizer
Scope: normalize severity, dependencies, fix ordering, P0/P1 blockers, regression sequencing and release gates.
Primary stages: G0-G13.

20. UX Architect
Scope: customer, retailer and admin journeys; information architecture; consistency; friction; empty/error/loading states; responsive UX.
Primary stages: G2,G4,G5,G7,G8,G11.

## Execution Waves
Wave 0: Freeze V49 evidence and inventory.
Wave 1: Agents 1-5 — security.
Wave 2: Agents 6-10 — engineering/infrastructure.
Wave 3: Agents 11-16 — testing/evidence/accessibility.
Wave 4: Agents 18-20 — product/UX/prioritization.
Wave 5: Agent 17 — independent Reality Check.
Wave 6: Consolidate, deduplicate, rank, fix, retest, and update G0-G13 certification gates.

## Conflict Resolution
When agents disagree:
- preserve both observations;
- compare exact evidence;
- reproduce the disputed behavior;
- prefer runtime/integration evidence over static inference;
- if unresolved, keep the finding OPEN and rate conservatively.

## Current G3 Carry-Forward Findings
The program must carry forward the existing G3 evidence gaps:
- invalid access cookie public/protected endpoint runtime proof;
- malformed/wrong-signature/expired JWT HTTP proof;
- refresh-token reuse family revocation and concurrent race proof;
- customer IDOR proof;
- retailer/admin/super-admin HTTP role matrix proof;
- reverse-proxy rate-limit proof;
- production cookie/SameSite/CORS topology proof;
- independent Reality Checker acceptance.

## Certification Gates
A stage may be marked GREEN only when all material findings are RETEST-PASS and the required evidence exists. Any unresolved P0/P1 or failed critical regression test blocks certification. G13 certification requires independent Reality Checker acceptance after all earlier stage gates pass.

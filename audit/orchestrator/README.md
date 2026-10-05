# Wolfe Zero-Gap 20-Agent Orchestrator

## Purpose
This directory defines the deterministic control plane for the V49 -> G0-G13 audit. The runner executes the 20 Agency-Agent methodologies sequentially, stores structured findings, and stops on blocking evidence.

## Safety model
- Never write directly to `main`.
- Audit and proposed-fix work must occur on a dedicated branch.
- P0/P1 findings or failed verification stop the pipeline.
- Source inspection alone cannot produce GREEN.
- Agent output is evidence for review, not certification.
- Agent 17 (Reality Checker) is an independent acceptance gate.
- External model execution is opt-in and requires `OPENAI_API_KEY` in the execution environment; the repository does not contain or create secrets.

## Pipeline
1. Freeze baseline and collect inventory.
2. Run agents 1-5 (security).
3. Run agents 6-10 (engineering/infrastructure).
4. Run agents 11-16 (testing/evidence/accessibility).
5. Run agents 18-20 (product/UX/prioritization).
6. Run agent 17 independently.
7. Deduplicate findings.
8. Apply only approved fixes on the audit branch.
9. Run the required regression suite.
10. Produce certification status for G0-G13.

## Execution
The GitHub Actions workflow is manually dispatched. It intentionally fails fast when the model credential is absent rather than silently pretending that agents ran.

Required secret:
- `OPENAI_API_KEY`

Recommended permissions for the workflow token are limited to repository contents and pull requests. Do not grant secret administration permissions.

The runner is designed so the model returns JSON matching `finding-schema.json`. A future implementation can add provider adapters without changing the audit contract.

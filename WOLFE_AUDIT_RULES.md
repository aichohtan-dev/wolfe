# Wolfe Audit Rules

1. V49 is the baseline.
2. Follow G0 through G13 in order; do not use random checking as a substitute for gate coverage.
3. Use only these statuses: PASS / FAIL / FIXED / NOT VERIFIED / N/A.
4. NOT VERIFIED is never PASS.
5. Every finding needs evidence.
6. A critical gap triggers stop-the-line: Find → Fix → Regression Test → Re-check → PASS.
7. Do not close a finding because code merely looks correct; verify behavior or the strongest available evidence.
8. Do not duplicate findings. Map recurring observations to the existing master finding where applicable.
9. Do not silently change the baseline. Record any later fix/change separately.
10. Before final certification, independently re-check the final release state.
11. Final certification requires critical gates to be closed and all remaining NOT VERIFIED items explicitly resolved or accepted as N/A with evidence.

---
**Certification note:** Historical/archival report; not runtime certification evidence. See `docs/REPORTING_POLICY.md` and the current Zero-Gap Master Findings ledger for authoritative status.

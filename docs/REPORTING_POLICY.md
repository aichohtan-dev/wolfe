# Wolfe audit reporting policy

1. No report may claim runtime PASS unless the exact artifact was executed and the evidence is recorded (Maven tests, Docker/Compose health, PostgreSQL/Redis integration, or browser E2E as applicable).
2. Static source review and structural checks are reported separately as `STATIC PASS`.
3. Historical V1–V49 audit/fix reports are archival evidence, not certification evidence. The current `WOLFE_ZERO_GAP_FINDINGS_001_100_MASTER.md` ledger and the latest batch report are authoritative for current status.
4. If runtime evidence is unavailable, the row remains `RUNTIME NOT VERIFIED` even when the implementation is statically fixed.

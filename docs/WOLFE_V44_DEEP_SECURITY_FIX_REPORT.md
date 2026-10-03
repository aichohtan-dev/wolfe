# Wolfe v44 Deep Security Fix Report

Remaining v43 findings addressed:
- HttpOnly + SameSite cookie sessions; browser token persistence removed.
- Distributed login throttling now has subject, subject+IP, and IP limits.
- Multi-instance cart recovery uses a pessimistic database claim with expiry.
- PDF uploads are spooled to disk; queued jobs no longer retain 50MB byte arrays, and executor concurrency/queue are bounded.
- Returns carry item-level quantities and restock only the selected quantities.
- HSTS is enabled for HTTPS requests.
- Initial catalog hydration is limited to 24 products and admin operational lists are bounded.
- Dockerfile bases are digest-pinned; Compose requires immutable image references for Postgres/Redis.
- Published catalog media now has a persistent container volume.

Verification: Maven compile could not execute because the environment could not resolve repo.maven.apache.org. Frontend node_modules are unavailable, so a full TypeScript build could not execute. Static source checks and ZIP integrity are still performed.

---
**Certification note:** Historical/archival report. It is not runtime certification evidence. See `docs/REPORTING_POLICY.md` and the current Zero-Gap Master Findings ledger for authoritative certification status.

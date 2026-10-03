# Wolfe Consolidated Fix Batch 4

## Closed
- #33 serializer-backed variant JSON construction verified
- #54 public filter endpoint now has bounded 60-second cache
- #85 rate limiting changed from fixed-window counters to Redis sliding window

## Hardened / partial
- #63 cart distinct-line cap = 50; N+1 remains
- #64 cart variant price now BigDecimal; configuration addon/availability projection remains

## Verification
- 1–100 ledger preserved
- Java brace/structure scan passed
- No merge conflict markers
- Runtime Maven/Postgres/Redis/Docker/browser execution remains NOT VERIFIED

---
**Certification note:** Historical/archival report; not runtime certification evidence. See `docs/REPORTING_POLICY.md` and the current Zero-Gap Master Findings ledger for authoritative status.

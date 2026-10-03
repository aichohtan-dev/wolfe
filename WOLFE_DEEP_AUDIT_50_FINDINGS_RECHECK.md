# Wolfe — 50-Finding Deep Audit Recheck

Date: 2026-10-01
Baseline: V49 runtime-certification checkpoint
Method: static source/config review + YAML parsing + invariant assertions. Maven/Docker runtime remains NOT VERIFIED because Maven Central DNS is unavailable and Docker daemon is unavailable.

## Fixed in this batch

1. Proxy-aware client IP: Spring `forward-headers-strategy: native`; Nginx passes forwarded headers.
2. Published catalog media: public Spring resource authorization + Nginx `/catalog/published/` proxy/cache route.
3. Published volume: runtime image creates and owns both imports and published directories.
4. Compose `.env.example`: mandatory immutable POSTGRES_IMAGE/REDIS_IMAGE placeholders documented.
5. Successful login/register no longer clears the shared IP bucket.
6. Redis rate-limit outage now returns 503 rather than generic 500.
7. Settled/already-adjusted settlement cannot be superseded by reassignment.
8. Variant stock is checked/reserved with pessimistic locking and optimistic versioning; cancellation restores variant stock.
9. JWT known placeholder values are rejected at startup.
10. Return state machine is explicit and row-locked by the admin update path.
12. Multiple partial returns are supported; prior non-rejected returned quantities are capped against remaining quantities; old unique order index is removed by V46.
14. Order creation requires an Idempotency-Key; customer+key is persisted with a unique partial index and checkout generates a stable key for the request/retry.
15. Customer cancellation is blocked after retailer fulfillment acceptance.
16. Orders without an eligible retailer are retained as CONFIRMED/awaiting allocation rather than failing with an internal ops error.
19. Admin customer access endpoint added; lock/disable is available and role changes require SUPER_ADMIN; session version is incremented.
21. Logout now revokes only the presented refresh session instead of invalidating every device's access token.
26. Nginx `limit_req_status 429` and client-IP key alignment added.

## Explicitly pending / requires further design or runtime evidence

11. Refund gateway linkage / immutable refund reference: payment model is currently COD-only; this requires a deliberate refund/ledger contract rather than a cosmetic field.
13. Returned inventory disposition: SELLABLE vs DAMAGED/inspection workflow needs an explicit business state and retailer/global inventory model.
17. Full admin pagination across every list: existing 100-row caps remain; API/UI contract needs a coordinated pagination rollout.
18. Complete actor audit trail for every admin money/order/coupon/price mutation: partial audit exists, but a uniform interceptor/event model is still required.
20. Password reset/change, email verification, account deletion: separate token/security lifecycle work remains.
22. Logout without refresh cookie cannot revoke an already-issued access JWT before its 15-minute expiry; requires JWT revocation/JTI design if immediate invalidation is required.
23. Session cap/list UI: refresh sessions need device metadata and bounded active-session policy.
24. Order entity exposure: customer/admin endpoints still need complete DTO conversion.
25. API body-size policy: global Nginx limit reduced to 1MB; PDF upload endpoint needs a dedicated higher limit if routed through Nginx.
27. TLS/redirect/gzip/static caching depend on deployment edge; Nginx container is intentionally HTTP behind an external TLS terminator.
28-29. Backup media, encryption, retention, offsite copy and tested restore remain deployment tasks; existing scripts cover DB recovery only.
30. Post-settlement return clawback/negative payable workflow needs settlement accounting design.
31-35. Money rounding, bundle allocation cap, JSON serialization, shipping-threshold semantics and coupon-quote consumption semantics need dedicated business-rule tests.
36. JWT issuer/audience/kid/rotation design remains pending.
37. JWT customer lookup cache remains a performance optimization, not a correctness gap.
38. Multi-origin CORS configuration remains deployment-specific.
39. Redis ACL/password/maxmemory remains deployment hardening.
40-41. Scheduled-job leader election and restart-safe PDF job recovery remain operational/runtime work.
42-43. CI supply-chain/test-depth expansion remains pending.
44. Frontend raw backend error presentation remains pending UX hardening.
45. Springdoc is flag-disabled in production but still on classpath; profile-based dependency separation remains optional hardening.
47. ADMIN vs SUPER_ADMIN permission granularity remains pending authorization model design.
48. Remaining bare `orElseThrow()` handlers should be normalized to contextual 404s.
49. Media deletion semantics should be unified (soft-delete/retention policy).
50. COD abuse controls beyond account-level active-order cap need OTP/phone/address policy design.

## Runtime boundary

The above static fixes are not runtime-certified. Maven could not bootstrap because `repo.maven.apache.org` DNS resolution is unavailable in this environment; Docker/browser/real PostgreSQL/Redis execution is likewise unavailable.

---
**Certification note:** Historical/archival report; not runtime certification evidence. See `docs/REPORTING_POLICY.md` and the current Zero-Gap Master Findings ledger for authoritative status.

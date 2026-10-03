# Wolfe v38 Security/Core Fix Report

Source baseline: `wolfe-v38-idor-privilege-hardened`

## Fixed in this pass

1. Login rate-limit proxy identity
   - Auth registration/login rate-limit now uses the reverse-proxy resolved client IP (`X-Real-IP`, then first `X-Forwarded-For`, then socket address).
   - This prevents all users behind the Nginx proxy from sharing the backend container's single remote-address bucket.
   - Nginx already overwrites these headers from the actual client connection.

2. Order cancellation / inventory race
   - Added pessimistic write lock on the order row for status transitions.
   - Existing inventory row lock remains in place.
   - Admin stock read-modify-write now also uses the inventory row lock.

3. Retailer reassignment integrity
   - Assignment operation locks the order row.
   - Terminal orders cannot be reassigned.
   - Retailer must be ACTIVE + VERIFIED.
   - Reassigning to the already-active retailer is idempotent and does not reserve stock twice.
   - Previous active retailer reservation is released and assignment becomes REASSIGNED.
   - Previous settlement is marked ADJUSTED; already-settled payouts cannot be silently reassigned.
   - Fulfillment retailer ownership is updated through an explicit setter.
   - Fulfillment that has already started cannot be reassigned.
   - REASSIGNED assignments can no longer be accepted/rejected as if active.
   - Delivery marks eligibility only for the current retailer's settlement.

4. API exception contract
   - Added explicit mappings for malformed JSON, missing parameters, unsupported HTTP method, missing endpoint, oversized upload and IllegalStateException.
   - Unexpected 500s are now logged.
   - Frontend API errors prefer `body.message` and fall back to `body.error`.
   - Refresh failures no longer wipe tokens on transient/network errors; tokens are cleared on an actual 401 refresh response.

5. Coupon money-unit contract
   - Admin coupon UI treats fixed/min/max monetary inputs as rupees and converts them to paise before sending to the API.
   - Percentage coupons are validated as whole-number 1–100 values.
   - Admin UI now exposes minimum subtotal, maximum discount and usage limit fields.

6. Allocation evaluation null crash
   - Replaced nullable `Map.of(...)` construction with a mutable response map so an unassigned order can be returned safely.

## Verification

Static source invariants were checked after patching.

Automated Maven compile/tests could NOT be executed in this environment because Maven wrapper bootstrap requires `repo.maven.apache.org` and network/DNS access is unavailable. Frontend `node_modules` is not present, so npm build/typecheck could not be executed either.

No git push/commit was performed.

---
**Certification note:** Historical/archival report. It is not runtime certification evidence. See `docs/REPORTING_POLICY.md` and the current Zero-Gap Master Findings ledger for authoritative certification status.

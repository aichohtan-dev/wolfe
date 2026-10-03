# Wolfe V36 — Experience Contract Follow-up Fix

## Scope
Follow-up audit of V35 Admin Experience / 360 / 3D / AR / Hotspot changes.

## Confirmed finding
`AdminExperienceController.listHotspots()` called `HotspotRepository.findByVisualContentIdAndOrderByIdAsc(...)`, but the V35 repository only declared `findByVisualContentIdAndActiveTrueOrderByIdAsc(...)`.

This was a frontend/backend contract inconsistency and would prevent a clean backend compilation.

## Fixes
1. Added the repository method required by the admin controller to list **all** hotspots, including inactive records.
2. Preserved the active-only repository method used by the public customer experience endpoint.
3. Added admin DELETE endpoint for product visual assets.
4. Added matching `api.admin.deleteVisualAsset()` client function.
5. Added Admin Experience "Delete asset" action with confirmation and state refresh.

## Source checks
- Relevant Java/TypeScript braces balanced.
- Old missing repository method reference resolved.
- Public hotspot endpoint still uses active-only records.
- Admin hotspot listing uses all records so inactive records remain manageable.
- No Flyway migration changed or added.
- No commit/push.

## Runtime verification limitation
Full Maven/TypeScript dependency verification could not be executed in this environment because Maven dependency download is blocked by DNS/network and `node_modules` is not present in the source ZIP.

---
**Certification note:** Historical/archival report. It is not runtime certification evidence. See `docs/REPORTING_POLICY.md` and the current Zero-Gap Master Findings ledger for authoritative certification status.

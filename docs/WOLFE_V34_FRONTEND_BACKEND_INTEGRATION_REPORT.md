# Wolfe V34 Frontend/Backend Integration + Variant Safety Fix Report

## Fixes
1. Review creation endpoint is now explicitly authenticated; public access remains GET-only. Anonymous review POST can no longer reach controller logic and fail as a server error.
2. Product media backend now accepts `360`, matching the existing Admin Product Media UI option.
3. Product variant selection no longer silently falls back to a different variant combination. If a selected combination does not exist, the Add to Bag action is disabled until a valid combination is selected.
4. Variant products now require a selected variant for Add to Bag; the base product is no longer silently added when variants exist but no valid combination is selected.

## Findings discovered during this pass
- Review POST was effectively exposed by the broad public review matcher. Because the controller assumed authentication, anonymous requests could reach a null authentication path rather than receiving a clean authorization response.
- Product Media UI offered `360`, while backend validation accepted only `IMAGE` and `VIDEO`; this was a concrete frontend/backend contract mismatch.
- Product variant selection used a fallback variant when the exact attribute combination did not exist. This could place a different SKU/combination into the cart than the customer selected.

## Verification
- Source-level checks completed.
- No TODO/FIXME/HACK/XXX markers found.
- Full npm typecheck not executed in this isolated source package because `node_modules` is intentionally excluded from clean variants.
- Maven test/build not executed here because Maven dependencies are not available offline.

---
**Certification note:** Historical/archival report. It is not runtime certification evidence. See `docs/REPORTING_POLICY.md` and the current Zero-Gap Master Findings ledger for authoritative certification status.

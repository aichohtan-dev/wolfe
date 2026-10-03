#!/usr/bin/env bash
set -euo pipefail
RESULT="$(docker compose exec -T postgres psql -v ON_ERROR_STOP=1 -At -U "${POSTGRES_USER:-wolfe}" -d "${POSTGRES_DB:-wolfe}" <<'SQL'
BEGIN;
SELECT pg_advisory_xact_lock(hashtext('wolfe-v43-integrity-preflight'));
SELECT
  (SELECT count(*) FROM orders WHERE total < 0) +
  (SELECT count(*) FROM order_items WHERE line_net_amount < 0) +
  (SELECT count(*) FROM coupons WHERE discount_type='PERCENT' AND value > 100) +
  (SELECT count(*) FROM retailers WHERE commission_rate < 0 OR commission_rate > 100) +
  (SELECT count(*) FROM retailer_settlements WHERE wolfe_margin_amount + retailer_payable_amount <> gross_amount);
COMMIT;
SQL
)"
VIOLATIONS="$(printf '%s\n' "$RESULT" | grep -E '^[[:space:]]*[0-9]+[[:space:]]*
case "$VIOLATIONS" in ''|*[!0-9]*) echo "Invalid preflight result: $VIOLATIONS" >&2; exit 1;; esac
[ "$VIOLATIONS" -eq 0 ] || { echo "V43 preflight failed: $VIOLATIONS integrity violations" >&2; exit 1; }
echo "V43 preflight PASS: all integrity violation counts are zero."
 | tail -1 | tr -d '[:space:]')"
case "$VIOLATIONS" in ''|*[!0-9]*) echo "Invalid preflight result: $VIOLATIONS" >&2; exit 1;; esac
[ "$VIOLATIONS" -eq 0 ] || { echo "V43 preflight failed: $VIOLATIONS integrity violations" >&2; exit 1; }
echo "V43 preflight PASS: all integrity violation counts are zero."

-- V61: Backfill item-level quantities for return requests created before V35.
-- V35 introduced item_quantities_json but did not populate existing rows. A legacy
-- return with NULL/blank selection would otherwise make later return creation fail
-- while the application tries to parse the prior selection, and it would prevent
-- the system from enforcing per-item cumulative return limits for that order.
UPDATE return_requests rr
SET item_quantities_json = x.item_quantities_json
FROM (
    SELECT r.id,
           jsonb_object_agg(oi.id::text, oi.quantity ORDER BY oi.id)::text AS item_quantities_json
    FROM return_requests r
    JOIN order_items oi ON oi.order_id = r.order_id
    WHERE r.item_quantities_json IS NULL OR btrim(r.item_quantities_json) = ''
    GROUP BY r.id
) x
WHERE rr.id = x.id
  AND (rr.item_quantities_json IS NULL OR btrim(rr.item_quantities_json) = '');

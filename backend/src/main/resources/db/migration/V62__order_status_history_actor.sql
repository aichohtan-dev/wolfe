-- Preserve actor attribution for order lifecycle history. Existing rows are intentionally
-- left nullable and are surfaced as SYSTEM by the entity fallback.
ALTER TABLE order_status_history
    ADD COLUMN IF NOT EXISTS actor VARCHAR(100);

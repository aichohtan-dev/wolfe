-- v43 deep security/integrity hardening; never edit historical migrations.
-- Staged PDF import media is intentionally not served as public catalog media.
CREATE INDEX IF NOT EXISTS idx_settlements_order_retailer_id ON retailer_settlements(order_id, retailer_id, id DESC);
CREATE INDEX IF NOT EXISTS idx_inventory_movements_order_type ON inventory_movements(order_id, movement_type, created_at);

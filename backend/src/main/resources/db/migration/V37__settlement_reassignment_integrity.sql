-- V37: Allow historical adjusted settlements while enforcing one active settlement per order/retailer.
-- V32 accidentally made the pair globally unique, which blocks A -> B -> A reassignment.
DROP INDEX IF EXISTS ux_retailer_settlement_order_retailer;
CREATE UNIQUE INDEX IF NOT EXISTS ux_retailer_settlement_order_retailer_active
    ON retailer_settlements(order_id, retailer_id)
    WHERE status <> 'ADJUSTED';
CREATE INDEX IF NOT EXISTS idx_retailer_settlement_order_retailer_history
    ON retailer_settlements(order_id, retailer_id, id DESC);

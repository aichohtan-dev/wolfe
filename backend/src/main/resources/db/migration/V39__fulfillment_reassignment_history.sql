-- V39: Make retailer fulfillment history append-only for A -> B -> A reassignment.
-- Historical rows may repeat the same order/retailer pair; only one active fulfillment may exist per order.
ALTER TABLE fulfillments DROP CONSTRAINT IF EXISTS uq_fulfillment_order_retailer;
DROP INDEX IF EXISTS ux_fulfillments_order_active;
CREATE INDEX IF NOT EXISTS idx_fulfillments_order_retailer_history
    ON fulfillments(order_id, retailer_id, id DESC);
CREATE UNIQUE INDEX IF NOT EXISTS ux_fulfillments_order_active
    ON fulfillments(order_id)
    WHERE status NOT IN ('CANCELLED','FAILED_DELIVERY','REASSIGNED','RETURNED');

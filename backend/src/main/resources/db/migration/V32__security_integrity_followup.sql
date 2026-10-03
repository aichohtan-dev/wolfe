-- V32: Security/integrity follow-up. Never modify historical migrations.
CREATE INDEX IF NOT EXISTS idx_product_configurations_customer ON product_configurations(customer_id);
CREATE UNIQUE INDEX IF NOT EXISTS ux_fulfillments_order_active ON fulfillments(order_id) WHERE status NOT IN ('CANCELLED','FAILED_DELIVERY','RETURNED');
ALTER TABLE retailer_inventory ADD CONSTRAINT ck_retailer_inventory_available CHECK (available_stock = physical_stock - reserved_stock);
ALTER TABLE retailer_inventory ADD CONSTRAINT ck_retailer_inventory_reserved CHECK (reserved_stock <= physical_stock);
ALTER TABLE retailer_settlements ADD COLUMN IF NOT EXISTS cash_expected_amount BIGINT NOT NULL DEFAULT 0;
ALTER TABLE retailer_settlements ADD COLUMN IF NOT EXISTS cash_collected_amount BIGINT NOT NULL DEFAULT 0;
ALTER TABLE retailer_settlements ADD COLUMN IF NOT EXISTS cash_deposited_amount BIGINT NOT NULL DEFAULT 0;
ALTER TABLE retailer_settlements ADD COLUMN IF NOT EXISTS cash_reconciliation_status VARCHAR(30) NOT NULL DEFAULT 'NOT_REQUIRED';
ALTER TABLE retailer_settlements ADD COLUMN IF NOT EXISTS cash_reconciliation_reference VARCHAR(100);
ALTER TABLE return_requests ADD COLUMN IF NOT EXISTS restock_status VARCHAR(30) NOT NULL DEFAULT 'NOT_PROCESSED';
ALTER TABLE return_requests ADD COLUMN IF NOT EXISTS settlement_adjustment_status VARCHAR(30) NOT NULL DEFAULT 'NOT_PROCESSED';
ALTER TABLE return_requests ADD COLUMN IF NOT EXISTS completed_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE customers ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE customers ADD COLUMN IF NOT EXISTS locked BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE retailer_settlements ADD COLUMN IF NOT EXISTS adjustment_amount BIGINT NOT NULL DEFAULT 0;

DELETE FROM retailer_settlements a USING retailer_settlements b
WHERE a.id < b.id AND a.order_id = b.order_id AND a.retailer_id = b.retailer_id;
CREATE UNIQUE INDEX IF NOT EXISTS ux_retailer_settlement_order_retailer ON retailer_settlements(order_id, retailer_id);
ALTER TABLE coupons ADD COLUMN IF NOT EXISTS per_customer_usage_limit INTEGER;

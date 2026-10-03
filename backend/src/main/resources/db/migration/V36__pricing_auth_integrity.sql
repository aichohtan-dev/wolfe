-- V33: final pricing/auth/inventory integrity hardening. Never modify historical migrations.
UPDATE product_variants SET sku = 'WLF-V-' || id WHERE sku IS NULL OR trim(sku) = '';
ALTER TABLE product_variants ALTER COLUMN sku SET NOT NULL;
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS coupon_discount BIGINT NOT NULL DEFAULT 0;
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS line_net_amount BIGINT NOT NULL DEFAULT 0;
UPDATE order_items SET line_net_amount = unit_price * quantity WHERE line_net_amount = 0;
CREATE INDEX IF NOT EXISTS idx_orders_customer_coupon_status ON orders(customer_id, coupon_code, status);
CREATE INDEX IF NOT EXISTS idx_retailer_settlements_cash_status ON retailer_settlements(cash_reconciliation_status, status);

UPDATE retailer_settlements rs SET cash_expected_amount = o.total, cash_reconciliation_status = 'PENDING' FROM orders o WHERE rs.order_id = o.id AND rs.cash_expected_amount = 0;

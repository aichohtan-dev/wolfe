CREATE INDEX IF NOT EXISTS idx_orders_status_created ON orders(status, created_at desc);

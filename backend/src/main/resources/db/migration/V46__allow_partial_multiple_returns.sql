ALTER TABLE return_requests DROP CONSTRAINT IF EXISTS uq_return_requests_order;
DROP INDEX IF EXISTS uq_return_requests_order;
CREATE INDEX IF NOT EXISTS idx_return_requests_order_id ON return_requests(order_id);

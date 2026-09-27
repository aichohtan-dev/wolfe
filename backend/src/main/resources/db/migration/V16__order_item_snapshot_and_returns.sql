ALTER TABLE order_items ADD COLUMN IF NOT EXISTS product_name VARCHAR(300);
UPDATE order_items oi SET product_name = COALESCE((SELECT p.name FROM products p WHERE p.id=oi.product_id), 'Wolfe product') WHERE product_name IS NULL;
ALTER TABLE order_items ALTER COLUMN product_name SET NOT NULL;
CREATE TABLE IF NOT EXISTS return_requests (
 id BIGSERIAL PRIMARY KEY,
 order_id VARCHAR(64) NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
 customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
 reason VARCHAR(1000) NOT NULL,
 status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_return_requests_order ON return_requests(order_id);
CREATE INDEX IF NOT EXISTS idx_return_requests_customer ON return_requests(customer_id, created_at DESC);

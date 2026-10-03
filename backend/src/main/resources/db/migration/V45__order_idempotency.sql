ALTER TABLE orders ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(100);
CREATE UNIQUE INDEX IF NOT EXISTS uk_orders_customer_idempotency ON orders(customer_id, idempotency_key) WHERE idempotency_key IS NOT NULL;

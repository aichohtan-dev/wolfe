ALTER TABLE orders ADD COLUMN payment_method VARCHAR(32) NOT NULL DEFAULT 'COD';
ALTER TABLE orders ADD COLUMN customer_name VARCHAR(200) NOT NULL DEFAULT 'Customer';
ALTER TABLE orders ADD COLUMN customer_email VARCHAR(320) NOT NULL DEFAULT 'unknown@example.invalid';
ALTER TABLE orders ADD COLUMN phone VARCHAR(30) NOT NULL DEFAULT 'unknown';
ALTER TABLE orders ADD COLUMN address VARCHAR(1000) NOT NULL DEFAULT 'Not provided';
ALTER TABLE orders ADD COLUMN city VARCHAR(120) NOT NULL DEFAULT 'Not provided';
ALTER TABLE orders ADD COLUMN pincode VARCHAR(20) NOT NULL DEFAULT '000000';
CREATE INDEX IF NOT EXISTS idx_orders_customer_created ON orders(customer_id, created_at DESC);

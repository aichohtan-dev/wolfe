ALTER TABLE customers ADD COLUMN IF NOT EXISTS phone VARCHAR(30);
CREATE TABLE IF NOT EXISTS customer_addresses (
 id BIGSERIAL PRIMARY KEY,
 customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
 label VARCHAR(80) NOT NULL,
 recipient_name VARCHAR(200) NOT NULL,
 phone VARCHAR(30) NOT NULL,
 address VARCHAR(1000) NOT NULL,
 city VARCHAR(120) NOT NULL,
 state VARCHAR(120) NOT NULL,
 pincode VARCHAR(20) NOT NULL,
 is_default BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_customer_addresses_customer ON customer_addresses(customer_id, is_default DESC, id DESC);

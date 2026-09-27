ALTER TABLE return_requests ADD COLUMN IF NOT EXISTS refund_amount BIGINT NOT NULL DEFAULT 0;
ALTER TABLE return_requests ADD COLUMN IF NOT EXISTS refund_status VARCHAR(32) NOT NULL DEFAULT 'NOT_REQUESTED';
ALTER TABLE return_requests ADD COLUMN IF NOT EXISTS admin_note VARCHAR(1000);
CREATE TABLE IF NOT EXISTS order_status_history (
 id BIGSERIAL PRIMARY KEY,
 order_id VARCHAR(64) NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
 status VARCHAR(32) NOT NULL,
 note VARCHAR(500),
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_order_status_history_order ON order_status_history(order_id, created_at ASC);
CREATE TABLE IF NOT EXISTS customer_notifications (
 id BIGSERIAL PRIMARY KEY,
 customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
 type VARCHAR(40) NOT NULL,
 title VARCHAR(200) NOT NULL,
 message VARCHAR(1000) NOT NULL,
 reference_type VARCHAR(40),
 reference_id VARCHAR(100),
 read_at TIMESTAMPTZ,
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_customer_notifications_customer ON customer_notifications(customer_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_customer_notifications_unread ON customer_notifications(customer_id, read_at);

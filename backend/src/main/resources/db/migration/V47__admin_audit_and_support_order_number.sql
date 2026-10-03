CREATE TABLE IF NOT EXISTS admin_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    actor_id BIGINT,
    actor_email VARCHAR(120) NOT NULL,
    method VARCHAR(12) NOT NULL,
    path VARCHAR(500) NOT NULL,
    status_code INTEGER NOT NULL,
    action VARCHAR(80) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_admin_audit_created_at ON admin_audit_logs(created_at);
CREATE INDEX IF NOT EXISTS idx_admin_audit_actor ON admin_audit_logs(actor_id);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS support_number VARCHAR(16);
UPDATE orders SET support_number = 'WLF-' || substr(md5(id), 1, 8) WHERE support_number IS NULL;
ALTER TABLE orders ALTER COLUMN support_number SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS ux_orders_support_number ON orders(support_number);

-- Session/device management metadata. Existing rows are valid with null device labels.
ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS device_label VARCHAR(120);
CREATE INDEX IF NOT EXISTS idx_refresh_customer_active_created ON refresh_tokens(customer_id, revoked_at, expires_at, created_at DESC);

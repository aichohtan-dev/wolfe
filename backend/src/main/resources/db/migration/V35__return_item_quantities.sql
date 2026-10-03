ALTER TABLE return_requests ADD COLUMN IF NOT EXISTS item_quantities_json TEXT;
ALTER TABLE cart_recovery ADD COLUMN IF NOT EXISTS reminder_claimed_at TIMESTAMPTZ NULL;
CREATE INDEX IF NOT EXISTS idx_cart_recovery_claim ON cart_recovery(reminder_sent, reminder_claimed_at, last_activity);

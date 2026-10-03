-- Explicit retailer recovery when a return exceeds the payable amount.
ALTER TABLE retailer_settlements ADD COLUMN IF NOT EXISTS recovery_due_amount BIGINT NOT NULL DEFAULT 0;
CREATE INDEX IF NOT EXISTS idx_retailer_settlement_recovery_due ON retailer_settlements(status, recovery_due_amount);

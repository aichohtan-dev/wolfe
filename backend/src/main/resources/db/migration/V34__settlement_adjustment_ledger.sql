CREATE TABLE IF NOT EXISTS retailer_settlement_adjustments (
  id BIGSERIAL PRIMARY KEY, settlement_id BIGINT NOT NULL REFERENCES retailer_settlements(id), order_id VARCHAR(50) NOT NULL,
  retailer_id BIGINT NOT NULL REFERENCES retailers(id), amount BIGINT NOT NULL CHECK (amount >= 0), type VARCHAR(80) NOT NULL,
  note VARCHAR(500), created_by VARCHAR(150) NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_settlement_adj_order ON retailer_settlement_adjustments(order_id);
CREATE INDEX IF NOT EXISTS idx_settlement_adj_settlement ON retailer_settlement_adjustments(settlement_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_settlement_return_adjustment ON retailer_settlement_adjustments(settlement_id, type) WHERE type = 'RETURN';

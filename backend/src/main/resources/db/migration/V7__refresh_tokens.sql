CREATE TABLE refresh_tokens (
  id BIGSERIAL PRIMARY KEY,
  customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
  token_hash VARCHAR(64) NOT NULL UNIQUE,
  expires_at TIMESTAMPTZ NOT NULL,
  revoked_at TIMESTAMPTZ NULL
);
CREATE INDEX idx_refresh_customer ON refresh_tokens(customer_id);
CREATE INDEX idx_refresh_expires ON refresh_tokens(expires_at);

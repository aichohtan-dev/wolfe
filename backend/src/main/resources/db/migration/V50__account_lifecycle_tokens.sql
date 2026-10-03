-- Account lifecycle: email verification and password-reset token hashes.
ALTER TABLE customers ADD COLUMN IF NOT EXISTS email_verified BOOLEAN NOT NULL DEFAULT TRUE;
CREATE TABLE IF NOT EXISTS account_tokens (
 id BIGSERIAL PRIMARY KEY, customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
 type VARCHAR(20) NOT NULL, token_hash VARCHAR(64) NOT NULL UNIQUE, expires_at TIMESTAMP WITH TIME ZONE NOT NULL, used_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX IF NOT EXISTS idx_account_token_customer_type ON account_tokens(customer_id,type);
CREATE INDEX IF NOT EXISTS idx_account_token_expires ON account_tokens(expires_at);

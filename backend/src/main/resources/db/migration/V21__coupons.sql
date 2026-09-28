CREATE TABLE coupons (
 id BIGSERIAL PRIMARY KEY,
 code VARCHAR(40) NOT NULL UNIQUE,
 discount_type VARCHAR(12) NOT NULL,
 value BIGINT NOT NULL,
 minimum_subtotal BIGINT NOT NULL DEFAULT 0,
 maximum_discount BIGINT NOT NULL DEFAULT 0,
 usage_limit INTEGER,
 used_count INTEGER NOT NULL DEFAULT 0,
 active BOOLEAN NOT NULL DEFAULT TRUE,
 starts_at TIMESTAMPTZ,
 expires_at TIMESTAMPTZ
);
CREATE INDEX idx_coupons_active_code ON coupons(code, active);

ALTER TABLE orders ADD COLUMN discount_amount BIGINT NOT NULL DEFAULT 0;
ALTER TABLE orders ADD COLUMN coupon_code VARCHAR(40);

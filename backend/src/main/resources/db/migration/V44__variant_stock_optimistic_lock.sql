ALTER TABLE product_variants
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE product_variants
    ADD CONSTRAINT ck_product_variant_stock_nonnegative CHECK (stock_quantity >= 0);

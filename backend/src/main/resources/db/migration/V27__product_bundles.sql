CREATE TABLE product_bundles (
 id BIGSERIAL PRIMARY KEY,
 slug VARCHAR(120) NOT NULL UNIQUE,
 name VARCHAR(200) NOT NULL,
 description VARCHAR(1000),
 discount_type VARCHAR(20) NOT NULL DEFAULT 'PERCENT',
 discount_value NUMERIC(12,2) NOT NULL DEFAULT 0,
 active BOOLEAN NOT NULL DEFAULT TRUE,
 CONSTRAINT product_bundles_discount_type_ck CHECK (discount_type IN ('PERCENT','FIXED')),
 CONSTRAINT product_bundles_discount_value_ck CHECK (discount_value >= 0)
);
CREATE TABLE product_bundle_items (
 id BIGSERIAL PRIMARY KEY,
 bundle_id BIGINT NOT NULL REFERENCES product_bundles(id) ON DELETE CASCADE,
 product_id BIGINT NOT NULL REFERENCES products(id),
 quantity INTEGER NOT NULL DEFAULT 1,
 CONSTRAINT product_bundle_items_quantity_ck CHECK (quantity > 0),
 CONSTRAINT product_bundle_items_unique UNIQUE(bundle_id,product_id)
);
CREATE INDEX idx_product_bundle_items_bundle ON product_bundle_items(bundle_id);
CREATE INDEX idx_product_bundle_items_product ON product_bundle_items(product_id);
ALTER TABLE order_items ADD COLUMN bundle_id BIGINT REFERENCES product_bundles(id);
ALTER TABLE order_items ADD COLUMN bundle_discount BIGINT NOT NULL DEFAULT 0;
CREATE INDEX idx_order_items_bundle ON order_items(bundle_id);

ALTER TABLE products ADD COLUMN material VARCHAR(100) NOT NULL DEFAULT 'Metal';
ALTER TABLE products ADD COLUMN color VARCHAR(100) NOT NULL DEFAULT 'Brass';
ALTER TABLE products ADD COLUMN style VARCHAR(100) NOT NULL DEFAULT 'Modern';
CREATE INDEX idx_products_attributes ON products(active, category, material, color, style, finish);
CREATE TABLE product_variants (
 id BIGSERIAL PRIMARY KEY,
 product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
 option_name VARCHAR(120) NOT NULL,
 option_value VARCHAR(180) NOT NULL,
 sku VARCHAR(120) UNIQUE,
 price_override NUMERIC(12,2),
 active BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE INDEX idx_product_variants_product ON product_variants(product_id);

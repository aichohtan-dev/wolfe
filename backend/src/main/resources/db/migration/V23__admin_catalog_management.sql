CREATE TABLE IF NOT EXISTS product_categories (
 id BIGSERIAL PRIMARY KEY,
 name VARCHAR(120) NOT NULL UNIQUE,
 active BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE IF NOT EXISTS product_collections (
 id BIGSERIAL PRIMARY KEY,
 name VARCHAR(120) NOT NULL UNIQUE,
 description VARCHAR(500),
 active BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE IF NOT EXISTS product_collection_items (
 collection_id BIGINT NOT NULL REFERENCES product_collections(id) ON DELETE CASCADE,
 product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
 PRIMARY KEY(collection_id,product_id)
);
CREATE TABLE IF NOT EXISTS product_media (
 id BIGSERIAL PRIMARY KEY,
 product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
 type VARCHAR(20) NOT NULL,
 url VARCHAR(2000) NOT NULL,
 alt_text VARCHAR(500),
 sort_order INTEGER NOT NULL DEFAULT 0,
 active BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE INDEX IF NOT EXISTS idx_product_media_product ON product_media(product_id,sort_order);
INSERT INTO product_categories(name,active)
SELECT DISTINCT category,TRUE FROM products p WHERE category IS NOT NULL AND trim(category)<>''
ON CONFLICT (name) DO NOTHING;

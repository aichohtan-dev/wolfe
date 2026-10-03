-- PostgreSQL trigram indexes make the existing escaped contains-search predictable at scale.
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX IF NOT EXISTS idx_products_name_trgm ON products USING gin (lower(name) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_products_slug_trgm ON products USING gin (lower(slug) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_products_description_trgm ON products USING gin (lower(description) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_products_brand_name_trgm ON products USING gin (lower(brand_name) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_products_subcategory_trgm ON products USING gin (lower(subcategory) gin_trgm_ops);

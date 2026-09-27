ALTER TABLE products ADD COLUMN featured BOOLEAN NOT NULL DEFAULT FALSE;
CREATE INDEX idx_products_featured_active ON products(active, featured, sort_order);

ALTER TABLE products ADD COLUMN media_urls VARCHAR(6000);
UPDATE products SET media_urls = image_url WHERE media_urls IS NULL AND image_url IS NOT NULL;
CREATE INDEX idx_products_category_active ON products(category, active, sort_order);

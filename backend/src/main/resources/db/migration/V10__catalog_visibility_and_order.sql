ALTER TABLE products ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE products ADD COLUMN sort_order INTEGER NOT NULL DEFAULT 0;
UPDATE products SET sort_order = CASE slug
  WHEN 'w-01' THEN 10 WHEN 'w-02' THEN 20 WHEN 'w-03' THEN 30
  WHEN 'w-04' THEN 40 WHEN 'w-05' THEN 50 WHEN 'w-06' THEN 60 ELSE sort_order END;
CREATE INDEX idx_products_active_order ON products(active, sort_order, name);

ALTER TABLE products ADD COLUMN image_url VARCHAR(1000);
UPDATE products SET image_url = CASE slug
  WHEN 'w-01' THEN '/catalog/brass-01.jpg'
  WHEN 'w-02' THEN '/catalog/brass-02.jpg'
  WHEN 'w-03' THEN '/catalog/brass-03.jpg'
  WHEN 'w-04' THEN '/catalog/brass-04.jpg'
  WHEN 'w-05' THEN '/catalog/brass-05.jpg'
  WHEN 'w-06' THEN '/catalog/brass-06.jpg'
  ELSE image_url END;

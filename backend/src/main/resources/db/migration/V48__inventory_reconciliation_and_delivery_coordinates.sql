ALTER TABLE retailers ADD COLUMN IF NOT EXISTS latitude NUMERIC(9,6);
ALTER TABLE retailers ADD COLUMN IF NOT EXISTS longitude NUMERIC(9,6);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS latitude NUMERIC(9,6);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS longitude NUMERIC(9,6);
ALTER TABLE retailers ADD CONSTRAINT ck_retailer_coordinates_lat CHECK (latitude IS NULL OR latitude BETWEEN -90 AND 90);
ALTER TABLE retailers ADD CONSTRAINT ck_retailer_coordinates_lon CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180);
ALTER TABLE orders ADD CONSTRAINT ck_order_coordinates_lat CHECK (latitude IS NULL OR latitude BETWEEN -90 AND 90);
ALTER TABLE orders ADD CONSTRAINT ck_order_coordinates_lon CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180);

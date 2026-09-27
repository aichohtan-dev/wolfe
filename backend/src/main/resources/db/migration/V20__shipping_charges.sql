ALTER TABLE orders ADD COLUMN subtotal BIGINT NOT NULL DEFAULT 0;
ALTER TABLE orders ADD COLUMN shipping_fee BIGINT NOT NULL DEFAULT 0;
ALTER TABLE orders ADD COLUMN shipping_method VARCHAR(32) NOT NULL DEFAULT 'STANDARD';

UPDATE orders SET subtotal = total, shipping_fee = 0 WHERE subtotal = 0;

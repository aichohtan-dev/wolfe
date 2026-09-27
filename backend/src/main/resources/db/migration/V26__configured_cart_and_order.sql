ALTER TABLE product_configurations ADD COLUMN selected_accessory_id BIGINT REFERENCES accessory_options(id);
ALTER TABLE product_configurations ADD COLUMN addon_price BIGINT NOT NULL DEFAULT 0;
ALTER TABLE product_configurations ADD COLUMN base_price BIGINT NOT NULL DEFAULT 0;
ALTER TABLE order_items ADD COLUMN configuration_token VARCHAR(32);
ALTER TABLE order_items ADD COLUMN configuration_json VARCHAR(12000);
CREATE INDEX idx_order_item_configuration ON order_items(configuration_token);

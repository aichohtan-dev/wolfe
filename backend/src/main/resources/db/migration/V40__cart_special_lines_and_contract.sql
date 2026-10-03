-- V40: Persist bundle/configuration identity in server carts so cart recovery is complete.
ALTER TABLE cart_items ADD COLUMN IF NOT EXISTS bundle_id BIGINT;
ALTER TABLE cart_items ADD COLUMN IF NOT EXISTS configuration_token VARCHAR(64);

ALTER TABLE cart_items
    ADD CONSTRAINT fk_cart_items_bundle
    FOREIGN KEY (bundle_id) REFERENCES product_bundles(id);

ALTER TABLE cart_items
    ADD CONSTRAINT fk_cart_items_configuration
    FOREIGN KEY (configuration_token) REFERENCES product_configurations(share_token);

DROP INDEX IF EXISTS ux_cart_customer_product_variant;
CREATE UNIQUE INDEX IF NOT EXISTS ux_cart_customer_product_variant_bundle_config
    ON cart_items (customer_id, product_id, COALESCE(variant_id, 0), COALESCE(bundle_id, 0), COALESCE(configuration_token, ''));
CREATE INDEX IF NOT EXISTS idx_cart_bundle ON cart_items(bundle_id);
CREATE INDEX IF NOT EXISTS idx_cart_configuration ON cart_items(configuration_token);

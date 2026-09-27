CREATE TABLE product_spin_frames (
 id BIGSERIAL PRIMARY KEY,
 product_id BIGINT NOT NULL REFERENCES products(id),
 image_url VARCHAR(1200) NOT NULL,
 sort_order INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX idx_spin_product_order ON product_spin_frames(product_id, sort_order);
CREATE TABLE product_visual_assets (
 id BIGSERIAL PRIMARY KEY,
 product_id BIGINT NOT NULL UNIQUE REFERENCES products(id),
 model_url VARCHAR(1200),
 ar_url VARCHAR(1200),
 poster_url VARCHAR(1200),
 active BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE visual_hotspots (
 id BIGSERIAL PRIMARY KEY,
 visual_content_id BIGINT NOT NULL REFERENCES visual_contents(id),
 label VARCHAR(120) NOT NULL,
 target_slug VARCHAR(120) NOT NULL,
 x DOUBLE PRECISION NOT NULL DEFAULT 50,
 y DOUBLE PRECISION NOT NULL DEFAULT 50,
 active BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE INDEX idx_hotspot_visual ON visual_hotspots(visual_content_id, active);
CREATE TABLE product_configurations (
 id BIGSERIAL PRIMARY KEY,
 share_token VARCHAR(32) NOT NULL UNIQUE,
 customer_id BIGINT,
 product_id BIGINT NOT NULL REFERENCES products(id),
 config_json VARCHAR(12000) NOT NULL,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_configuration_customer ON product_configurations(customer_id, created_at);
CREATE TABLE recently_viewed (
 id BIGSERIAL PRIMARY KEY,
 customer_id BIGINT NOT NULL REFERENCES customers(id),
 product_id BIGINT NOT NULL REFERENCES products(id),
 viewed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE(customer_id, product_id)
);
CREATE INDEX idx_recent_customer ON recently_viewed(customer_id, viewed_at DESC);
CREATE TABLE back_in_stock_subscriptions (
 id BIGSERIAL PRIMARY KEY,
 customer_id BIGINT NOT NULL REFERENCES customers(id),
 product_id BIGINT NOT NULL REFERENCES products(id),
 active BOOLEAN NOT NULL DEFAULT TRUE,
 created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE(customer_id, product_id)
);
CREATE INDEX idx_stock_subscription_product ON back_in_stock_subscriptions(product_id, active);
CREATE TABLE cart_recovery (
 customer_id BIGINT PRIMARY KEY REFERENCES customers(id),
 last_activity TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
 reminder_sent BOOLEAN NOT NULL DEFAULT FALSE,
 recovery_token VARCHAR(1000)
);

ALTER TABLE quote_requests ADD COLUMN configuration_id BIGINT REFERENCES product_configurations(id);
CREATE INDEX idx_quote_configuration ON quote_requests(configuration_id);

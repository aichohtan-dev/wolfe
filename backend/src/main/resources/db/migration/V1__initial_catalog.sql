CREATE TABLE products (
 id BIGSERIAL PRIMARY KEY,
 slug VARCHAR(180) NOT NULL UNIQUE,
 name VARCHAR(255) NOT NULL,
 price NUMERIC(12,2) NOT NULL,
 category VARCHAR(100) NOT NULL,
 finish VARCHAR(100) NOT NULL,
 description VARCHAR(2000)
);
CREATE INDEX idx_products_category ON products(category);
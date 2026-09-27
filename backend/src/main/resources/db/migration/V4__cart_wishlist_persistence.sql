CREATE TABLE cart_items (
  customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
  product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
  quantity INTEGER NOT NULL,
  PRIMARY KEY (customer_id, product_id),
  CHECK (quantity > 0)
);

CREATE TABLE wishlist_items (
  customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
  product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
  PRIMARY KEY (customer_id, product_id)
);

CREATE INDEX idx_cart_customer ON cart_items(customer_id);
CREATE INDEX idx_wishlist_customer ON wishlist_items(customer_id);

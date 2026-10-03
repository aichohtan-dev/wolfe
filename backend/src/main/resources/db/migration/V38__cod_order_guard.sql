-- V38: Fast lookup for the authenticated customer's active COD order limit.
CREATE INDEX IF NOT EXISTS idx_orders_customer_payment_status
    ON orders(customer_id, payment_method, status);

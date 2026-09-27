ALTER TABLE customers ADD COLUMN role VARCHAR(32) NOT NULL DEFAULT 'CUSTOMER';
CREATE INDEX idx_customers_role ON customers(role);

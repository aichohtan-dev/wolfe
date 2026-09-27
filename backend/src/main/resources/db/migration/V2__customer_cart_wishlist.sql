CREATE TABLE customers (
 id BIGSERIAL PRIMARY KEY,
 name VARCHAR(160) NOT NULL,
 email VARCHAR(255) NOT NULL UNIQUE,
 password_hash VARCHAR(255) NOT NULL
);
CREATE INDEX idx_customers_email ON customers(email);
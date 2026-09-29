-- V29: Invisible Local Retailer Network, Multi-Tenant Inventory, Stock Reservation Ledger, Dynamic Allocation, Fulfillment & Settlements

-- 1. Retailers
CREATE TABLE IF NOT EXISTS retailers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    owner_name VARCHAR(150),
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(50) NOT NULL,
    address VARCHAR(500) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL DEFAULT 'Rajasthan',
    pincode VARCHAR(20) NOT NULL,
    delivery_radius_km NUMERIC(5,2) NOT NULL DEFAULT 25.0,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    verification_status VARCHAR(50) NOT NULL DEFAULT 'VERIFIED',
    agreement_status VARCHAR(50) NOT NULL DEFAULT 'SIGNED',
    rating NUMERIC(3,2) NOT NULL DEFAULT 4.8,
    commission_rate NUMERIC(5,2) NOT NULL DEFAULT 10.0,
    user_id BIGINT REFERENCES customers(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_retailers_city ON retailers(city);
CREATE INDEX IF NOT EXISTS idx_retailers_status ON retailers(status, verification_status);
CREATE INDEX IF NOT EXISTS idx_retailers_user_id ON retailers(user_id);

-- 2. Retailer Service Areas
CREATE TABLE IF NOT EXISTS retailer_service_areas (
    id BIGSERIAL PRIMARY KEY,
    retailer_id BIGINT NOT NULL REFERENCES retailers(id) ON DELETE CASCADE,
    pincode VARCHAR(20) NOT NULL,
    city VARCHAR(100) NOT NULL,
    area_name VARCHAR(150),
    delivery_eta_hours INTEGER NOT NULL DEFAULT 24,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_retailer_pincode UNIQUE (retailer_id, pincode)
);

CREATE INDEX IF NOT EXISTS idx_service_areas_pincode ON retailer_service_areas(pincode, active);
CREATE INDEX IF NOT EXISTS idx_service_areas_retailer ON retailer_service_areas(retailer_id);

-- 3. Retailer Inventory
CREATE TABLE IF NOT EXISTS retailer_inventory (
    id BIGSERIAL PRIMARY KEY,
    retailer_id BIGINT NOT NULL REFERENCES retailers(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    variant_id BIGINT REFERENCES product_variants(id) ON DELETE CASCADE,
    sku VARCHAR(100) NOT NULL,
    physical_stock INTEGER NOT NULL DEFAULT 0 CHECK (physical_stock >= 0),
    reserved_stock INTEGER NOT NULL DEFAULT 0 CHECK (reserved_stock >= 0),
    available_stock INTEGER NOT NULL DEFAULT 0 CHECK (available_stock >= 0),
    low_stock_threshold INTEGER NOT NULL DEFAULT 5,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_retailer_sku UNIQUE (retailer_id, sku)
);

CREATE INDEX IF NOT EXISTS idx_ret_inv_retailer ON retailer_inventory(retailer_id);
CREATE INDEX IF NOT EXISTS idx_ret_inv_sku ON retailer_inventory(sku);
CREATE INDEX IF NOT EXISTS idx_ret_inv_prod_var ON retailer_inventory(product_id, variant_id);

-- 4. Inventory Audit Ledger (Movements)
CREATE TABLE IF NOT EXISTS inventory_movements (
    id BIGSERIAL PRIMARY KEY,
    retailer_id BIGINT NOT NULL REFERENCES retailers(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    variant_id BIGINT REFERENCES product_variants(id) ON DELETE SET NULL,
    sku VARCHAR(100) NOT NULL,
    previous_quantity INTEGER NOT NULL,
    quantity_changed INTEGER NOT NULL,
    new_quantity INTEGER NOT NULL,
    movement_type VARCHAR(50) NOT NULL,
    order_id VARCHAR(50),
    created_by VARCHAR(150),
    reason VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_inv_mov_retailer ON inventory_movements(retailer_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_inv_mov_sku ON inventory_movements(sku);
CREATE INDEX IF NOT EXISTS idx_inv_mov_order ON inventory_movements(order_id);

-- 5. Retailer Order Assignments
CREATE TABLE IF NOT EXISTS retailer_order_assignments (
    id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    retailer_id BIGINT NOT NULL REFERENCES retailers(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL DEFAULT 'ASSIGNED',
    assigned_by VARCHAR(100) NOT NULL DEFAULT 'AUTO_ALLOCATION',
    assigned_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    accepted_at TIMESTAMP WITH TIME ZONE,
    rejected_at TIMESTAMP WITH TIME ZONE,
    rejection_reason VARCHAR(500),
    notes VARCHAR(1000)
);

CREATE INDEX IF NOT EXISTS idx_assignments_order ON retailer_order_assignments(order_id);
CREATE INDEX IF NOT EXISTS idx_assignments_retailer ON retailer_order_assignments(retailer_id, status);

-- 6. Fulfillments
CREATE TABLE IF NOT EXISTS fulfillments (
    id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    retailer_id BIGINT NOT NULL REFERENCES retailers(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL DEFAULT 'ASSIGNED',
    tracking_number VARCHAR(100),
    courier_name VARCHAR(100) DEFAULT 'Wolfe Local Logistics',
    packed_at TIMESTAMP WITH TIME ZONE,
    shipped_at TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE,
    failed_at TIMESTAMP WITH TIME ZONE,
    failed_reason VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_fulfillment_order_retailer UNIQUE (order_id, retailer_id)
);

CREATE INDEX IF NOT EXISTS idx_fulfillments_order ON fulfillments(order_id);
CREATE INDEX IF NOT EXISTS idx_fulfillments_retailer_status ON fulfillments(retailer_id, status);

-- 7. Retailer Margin Rules
CREATE TABLE IF NOT EXISTS retailer_margin_rules (
    id BIGSERIAL PRIMARY KEY,
    retailer_id BIGINT REFERENCES retailers(id) ON DELETE CASCADE,
    category VARCHAR(120),
    product_id BIGINT REFERENCES products(id) ON DELETE CASCADE,
    variant_id BIGINT REFERENCES product_variants(id) ON DELETE CASCADE,
    margin_type VARCHAR(20) NOT NULL DEFAULT 'PERCENTAGE',
    margin_value NUMERIC(10,2) NOT NULL DEFAULT 10.0,
    priority INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_margin_rules_retailer ON retailer_margin_rules(retailer_id, active);
CREATE INDEX IF NOT EXISTS idx_margin_rules_cat ON retailer_margin_rules(category);

-- 8. Retailer Settlements
CREATE TABLE IF NOT EXISTS retailer_settlements (
    id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    retailer_id BIGINT NOT NULL REFERENCES retailers(id) ON DELETE CASCADE,
    gross_amount BIGINT NOT NULL,
    wolfe_margin_amount BIGINT NOT NULL,
    retailer_payable_amount BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    reference_number VARCHAR(100),
    settled_at TIMESTAMP WITH TIME ZONE,
    notes VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_settlements_retailer ON retailer_settlements(retailer_id, status);
CREATE INDEX IF NOT EXISTS idx_settlements_order ON retailer_settlements(order_id);

-- 9. Retailer Audit Logs
CREATE TABLE IF NOT EXISTS retailer_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    entity_name VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100) NOT NULL,
    action VARCHAR(100) NOT NULL,
    actor VARCHAR(150) NOT NULL,
    details VARCHAR(2000),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ret_audit_entity ON retailer_audit_logs(entity_name, entity_id);

-- 10. Seed Initial Local Retailer Network (Jodhpur, Jaipur, Delhi NCR, Mumbai)
INSERT INTO retailers (name, owner_name, email, phone, address, city, state, pincode, delivery_radius_km, status, verification_status, agreement_status, rating, commission_rate) VALUES
('Hinglaj Hardware & Plywood Depot', 'Ramesh Hinglaj', 'hinglaj.partner@wolfe.internal', '+91 98290 12345', '10B Road, Opp. Barmer Bhavan, Sardarpura', 'Jodhpur', 'Rajasthan', '342003', 35.0, 'ACTIVE', 'VERIFIED', 'SIGNED', 4.9, 12.0),
('Marwar Architectural Fittings & Hardware', 'Vikram Gehlot', 'marwar.fittings@wolfe.internal', '+91 98290 54321', 'Station Road, Ratanada', 'Jodhpur', 'Rajasthan', '342001', 25.0, 'ACTIVE', 'VERIFIED', 'SIGNED', 4.8, 10.0),
('Jaipur Royal Plywood & Laminate Hub', 'Anil Sharma', 'jaipur.hub@wolfe.internal', '+91 94140 88776', 'MI Road, Near Panch Batti', 'Jaipur', 'Rajasthan', '302001', 30.0, 'ACTIVE', 'VERIFIED', 'SIGNED', 4.7, 11.5),
('Apex Luxury Hardware & Kitchen Gallery', 'Sanjay Rathore', 'apex.hardware@wolfe.internal', '+91 98110 33445', 'Connaught Place, Barakhamba', 'New Delhi', 'Delhi', '110001', 20.0, 'ACTIVE', 'VERIFIED', 'SIGNED', 4.9, 10.0)
ON CONFLICT (email) DO NOTHING;

-- Seed Service Areas for Retailers
INSERT INTO retailer_service_areas (retailer_id, pincode, city, area_name, delivery_eta_hours, active)
SELECT r.id, p.pincode, p.city, p.area_name, p.eta, TRUE
FROM retailers r
CROSS JOIN (
    VALUES
    ('342001', 'Jodhpur', 'Ratanada & Old City', 12),
    ('342003', 'Jodhpur', 'Sardarpura & C-Road', 6),
    ('342008', 'Jodhpur', 'Pal Road & Shastri Nagar', 12),
    ('342005', 'Jodhpur', 'Residency & Air Force', 12)
) AS p(pincode, city, area_name, eta)
WHERE r.name = 'Hinglaj Hardware & Plywood Depot'
ON CONFLICT (retailer_id, pincode) DO NOTHING;

INSERT INTO retailer_service_areas (retailer_id, pincode, city, area_name, delivery_eta_hours, active)
SELECT r.id, p.pincode, p.city, p.area_name, p.eta, TRUE
FROM retailers r
CROSS JOIN (
    VALUES
    ('342001', 'Jodhpur', 'Station Road & Paota', 12),
    ('342003', 'Jodhpur', 'Sardarpura', 12),
    ('342006', 'Jodhpur', 'Basni Industrial Area', 18)
) AS p(pincode, city, area_name, eta)
WHERE r.name = 'Marwar Architectural Fittings & Hardware'
ON CONFLICT (retailer_id, pincode) DO NOTHING;

INSERT INTO retailer_service_areas (retailer_id, pincode, city, area_name, delivery_eta_hours, active)
SELECT r.id, p.pincode, p.city, p.area_name, p.eta, TRUE
FROM retailers r
CROSS JOIN (
    VALUES
    ('302001', 'Jaipur', 'MI Road & C-Scheme', 12),
    ('302015', 'Jaipur', 'Malviya Nagar & Tonk Road', 18),
    ('302020', 'Jaipur', 'Mansarovar', 24)
) AS p(pincode, city, area_name, eta)
WHERE r.name = 'Jaipur Royal Plywood & Laminate Hub'
ON CONFLICT (retailer_id, pincode) DO NOTHING;

INSERT INTO retailer_service_areas (retailer_id, pincode, city, area_name, delivery_eta_hours, active)
SELECT r.id, p.pincode, p.city, p.area_name, p.eta, TRUE
FROM retailers r
CROSS JOIN (
    VALUES
    ('110001', 'New Delhi', 'Connaught Place & Central', 12),
    ('110020', 'New Delhi', 'Okhla & South Delhi', 24),
    ('122001', 'Gurugram', 'Cyber City & DLF', 24)
) AS p(pincode, city, area_name, eta)
WHERE r.name = 'Apex Luxury Hardware & Kitchen Gallery'
ON CONFLICT (retailer_id, pincode) DO NOTHING;

-- Seed Initial Retailer Inventories for Products & Multi-Attribute Variants
-- Hinglaj Hardware stock
INSERT INTO retailer_inventory (retailer_id, product_id, variant_id, sku, physical_stock, reserved_stock, available_stock, low_stock_threshold)
SELECT r.id, pv.product_id, pv.id, pv.sku, 30, 0, 30, 5
FROM retailers r
CROSS JOIN product_variants pv
WHERE r.name = 'Hinglaj Hardware & Plywood Depot' AND pv.active = TRUE
ON CONFLICT (retailer_id, sku) DO NOTHING;

-- Also seed base product items for products without explicit variants or base inventory
INSERT INTO retailer_inventory (retailer_id, product_id, variant_id, sku, physical_stock, reserved_stock, available_stock, low_stock_threshold)
SELECT r.id, p.id, NULL, 'SKU-' || UPPER(p.slug), 25, 0, 25, 5
FROM retailers r
CROSS JOIN products p
WHERE r.name = 'Hinglaj Hardware & Plywood Depot'
  AND NOT EXISTS (SELECT 1 FROM product_variants pv WHERE pv.product_id = p.id)
ON CONFLICT (retailer_id, sku) DO NOTHING;

-- Marwar Fittings stock
INSERT INTO retailer_inventory (retailer_id, product_id, variant_id, sku, physical_stock, reserved_stock, available_stock, low_stock_threshold)
SELECT r.id, pv.product_id, pv.id, pv.sku, 20, 0, 20, 5
FROM retailers r
CROSS JOIN product_variants pv
WHERE r.name = 'Marwar Architectural Fittings & Hardware' AND pv.active = TRUE
ON CONFLICT (retailer_id, sku) DO NOTHING;

INSERT INTO retailer_inventory (retailer_id, product_id, variant_id, sku, physical_stock, reserved_stock, available_stock, low_stock_threshold)
SELECT r.id, p.id, NULL, 'SKU-' || UPPER(p.slug), 15, 0, 15, 5
FROM retailers r
CROSS JOIN products p
WHERE r.name = 'Marwar Architectural Fittings & Hardware'
  AND NOT EXISTS (SELECT 1 FROM product_variants pv WHERE pv.product_id = p.id)
ON CONFLICT (retailer_id, sku) DO NOTHING;

-- Seed Margin Rules
INSERT INTO retailer_margin_rules (retailer_id, category, margin_type, margin_value, priority, active)
VALUES
(NULL, 'Hardware', 'PERCENTAGE', 12.0, 1, TRUE),
(NULL, 'Plywood', 'PERCENTAGE', 8.0, 1, TRUE),
(NULL, 'Laminates', 'PERCENTAGE', 10.0, 1, TRUE),
(NULL, 'Kitchen Accessories', 'PERCENTAGE', 15.0, 1, TRUE)
ON CONFLICT DO NOTHING;

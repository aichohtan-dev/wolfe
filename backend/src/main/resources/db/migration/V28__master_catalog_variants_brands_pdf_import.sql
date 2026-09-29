-- V28: Master Catalog, Rich Variants, Brands, Subcategories, Order Variant Snapshots, and PDF Import Workflow

-- 1. Brands
CREATE TABLE IF NOT EXISTS brands (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    slug VARCHAR(150) NOT NULL UNIQUE,
    logo_url VARCHAR(1000),
    description VARCHAR(1000),
    website VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_brands_slug ON brands(slug);
CREATE INDEX IF NOT EXISTS idx_brands_active ON brands(active, sort_order);

-- 2. Subcategories
CREATE TABLE IF NOT EXISTS product_subcategories (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT REFERENCES product_categories(id) ON DELETE SET NULL,
    category_name VARCHAR(120) NOT NULL,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uq_cat_subcat UNIQUE (category_name, name)
);
CREATE INDEX IF NOT EXISTS idx_subcategories_cat ON product_subcategories(category_name, active);

-- 3. Update Categories & Replace Sunmica with Laminates
UPDATE product_categories SET name = 'Laminates' WHERE LOWER(name) IN ('sunmica', 'mica', 'sun mica');
UPDATE products SET category = 'Laminates' WHERE LOWER(category) IN ('sunmica', 'mica', 'sun mica');

INSERT INTO product_categories (name, active) VALUES
('Hardware', TRUE),
('Plywood', TRUE),
('Laminates', TRUE),
('Kitchen Accessories', TRUE)
ON CONFLICT (name) DO NOTHING;

-- 4. Alter products for brands, subcategories, dimensions, model number, attributes_json
ALTER TABLE products ADD COLUMN IF NOT EXISTS brand_id BIGINT REFERENCES brands(id) ON DELETE SET NULL;
ALTER TABLE products ADD COLUMN IF NOT EXISTS brand_name VARCHAR(120);
ALTER TABLE products ADD COLUMN IF NOT EXISTS subcategory VARCHAR(120);
ALTER TABLE products ADD COLUMN IF NOT EXISTS dimensions VARCHAR(200);
ALTER TABLE products ADD COLUMN IF NOT EXISTS model_number VARCHAR(120);
ALTER TABLE products ADD COLUMN IF NOT EXISTS attributes_json VARCHAR(12000);

CREATE INDEX IF NOT EXISTS idx_products_brand ON products(brand_id);
CREATE INDEX IF NOT EXISTS idx_products_subcategory ON products(subcategory);
CREATE INDEX IF NOT EXISTS idx_products_cat_sub ON products(category, subcategory);
CREATE INDEX IF NOT EXISTS idx_products_price_filter ON products(active, price);

-- 5. Alter product_variants for multi-attribute variant matrix
ALTER TABLE product_variants ADD COLUMN IF NOT EXISTS title VARCHAR(200);
ALTER TABLE product_variants ADD COLUMN IF NOT EXISTS color VARCHAR(100);
ALTER TABLE product_variants ADD COLUMN IF NOT EXISTS material VARCHAR(100);
ALTER TABLE product_variants ADD COLUMN IF NOT EXISTS size VARCHAR(100);
ALTER TABLE product_variants ADD COLUMN IF NOT EXISTS finish VARCHAR(100);
ALTER TABLE product_variants ADD COLUMN IF NOT EXISTS dimensions VARCHAR(100);
ALTER TABLE product_variants ADD COLUMN IF NOT EXISTS price NUMERIC(12,2);
ALTER TABLE product_variants ADD COLUMN IF NOT EXISTS stock_quantity INTEGER NOT NULL DEFAULT 50;
ALTER TABLE product_variants ADD COLUMN IF NOT EXISTS image_url VARCHAR(1000);
ALTER TABLE product_variants ADD COLUMN IF NOT EXISTS sort_order INTEGER NOT NULL DEFAULT 0;
ALTER TABLE product_variants ADD COLUMN IF NOT EXISTS attributes_json VARCHAR(4000);

CREATE INDEX IF NOT EXISTS idx_variants_product_active ON product_variants(product_id, active);
CREATE INDEX IF NOT EXISTS idx_variants_sku ON product_variants(sku);

-- 6. Alter order_items for historical variant snapshot
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS variant_id BIGINT;
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS variant_sku VARCHAR(120);
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS variant_title VARCHAR(255);
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS variant_attributes_json VARCHAR(4000);

-- 7. PDF Catalog Import Tables
CREATE TABLE IF NOT EXISTS pdf_import_jobs (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(1000),
    file_size BIGINT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    total_pages INTEGER DEFAULT 0,
    total_extracted INTEGER DEFAULT 0,
    error_message VARCHAR(2000),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_pdf_jobs_status ON pdf_import_jobs(status);

CREATE TABLE IF NOT EXISTS pdf_import_items (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT NOT NULL REFERENCES pdf_import_jobs(id) ON DELETE CASCADE,
    page_number INTEGER,
    name VARCHAR(255) NOT NULL,
    sku VARCHAR(120),
    brand VARCHAR(120),
    category VARCHAR(120),
    subcategory VARCHAR(120),
    price NUMERIC(12,2),
    finish VARCHAR(100),
    material VARCHAR(100),
    color VARCHAR(100),
    size VARCHAR(100),
    dimensions VARCHAR(200),
    description VARCHAR(2000),
    image_url VARCHAR(1000),
    media_urls VARCHAR(4000),
    extracted_variants_json VARCHAR(8000),
    attributes_json VARCHAR(8000),
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    confidence_score NUMERIC(3,2) DEFAULT 0.90,
    duplicate_product_id BIGINT REFERENCES products(id) ON DELETE SET NULL,
    notes VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_pdf_items_job ON pdf_import_items(job_id, status);
CREATE INDEX IF NOT EXISTS idx_pdf_items_sku ON pdf_import_items(sku);

-- 8. Seed Brands
INSERT INTO brands (name, slug, logo_url, description, website, active, sort_order) VALUES
('Wolfe Heritage', 'wolfe-heritage', '/wolfe-logo.png', 'Signature architectural hardware and curated interior materials by Wolfe.', 'https://wolfe.style', TRUE, 1),
('Hettich', 'hettich', '/brands/hettich.png', 'German precision engineering for drawer systems, hinges, and sliding hardware.', 'https://hettich.com', TRUE, 2),
('Hafele', 'hafele', '/brands/hafele.png', 'Global leader in architectural hardware and modular kitchen fittings.', 'https://hafele.com', TRUE, 3),
('Blum', 'blum', '/brands/blum.png', 'Premium lift-up, hinge, and runner systems for modern cabinetry.', 'https://blum.com', TRUE, 4),
('Greenply', 'greenply', '/brands/greenply.png', 'India''s premier plywood and structural wood manufacturing brand.', 'https://greenply.com', TRUE, 5),
('CenturyPly', 'centuryply', '/brands/centuryply.png', 'High-durability BWP marine plywood, calibrated boards, and veneers.', 'https://centuryply.com', TRUE, 6),
('Merino', 'merino', '/brands/merino.png', 'Exquisite decorative laminates, textures, and surface materials.', 'https://merinolaminates.com', TRUE, 7),
('Greenlam', 'greenlam', '/brands/greenlam.png', 'Innovative high-pressure decorative laminates and architectural surfaces.', 'https://greenlam.com', TRUE, 8),
('Godrej', 'godrej', '/brands/godrej.png', 'Trusted security solutions, architectural mortise locks, and door fittings.', 'https://godrej.com', TRUE, 9),
('Ebco', 'ebco', '/brands/ebco.png', 'Versatile furniture accessories, drawer slides, and kitchen organizers.', 'https://ebco.in', TRUE, 10)
ON CONFLICT (name) DO NOTHING;

-- 9. Seed Subcategories
INSERT INTO product_subcategories (category_name, name, slug, description, active, sort_order) VALUES
-- Hardware
('Hardware', 'Handles', 'handles', 'Door levers, cabinet pulls, and statement hardware.', TRUE, 1),
('Hardware', 'Knobs', 'knobs', 'Cabinet, wardrobe, and furniture knobs.', TRUE, 2),
('Hardware', 'Hooks', 'hooks', 'Sculptural and architectural wall hooks.', TRUE, 3),
('Hardware', 'Locks', 'locks', 'Mortise lock bodies, cylinders, and security fittings.', TRUE, 4),
('Hardware', 'Hinges', 'hinges', 'Concealed, butt, and hydraulic soft-close hinges.', TRUE, 5),
('Hardware', 'Door Accessories', 'door-accessories', 'Door stops, flush bolts, and pivot accessories.', TRUE, 6),

-- Plywood
('Plywood', 'Commercial Ply', 'commercial-ply', 'MR Grade moisture resistant commercial plywood.', TRUE, 1),
('Plywood', 'Marine / BWP Ply', 'marine-bwp-ply', 'Boiling water proof waterproof marine grade ply.', TRUE, 2),
('Plywood', 'Calibrated Ply', 'calibrated-ply', 'Quad-press calibrated uniform thickness core ply.', TRUE, 3),
('Plywood', 'Flexible Ply', 'flexible-ply', 'High-flexibility curved structural ply.', TRUE, 4),
('Plywood', 'Fire Retardant Ply', 'fire-retardant-ply', 'Treated fire-retardant safety plywood.', TRUE, 5),

-- Laminates
('Laminates', 'High Gloss', 'high-gloss', 'Reflective mirror and gloss finish laminates.', TRUE, 1),
('Laminates', 'Matte / Suede', 'matte-suede', 'Soft tactile suede and anti-fingerprint matte surfaces.', TRUE, 2),
('Laminates', 'Textured & Fluted', 'textured-fluted', 'Architectural ribbed, fluted, and embossed textures.', TRUE, 3),
('Laminates', 'Wood Grain', 'wood-grain', 'Natural veneer-replica authentic wood grains.', TRUE, 4),
('Laminates', 'Metallic & Acrylic', 'metallic-acrylic', 'Brushed brass, bronze, aluminium, and acrylic sheets.', TRUE, 5),

-- Kitchen Accessories
('Kitchen Accessories', 'Drawer & Sliding Systems', 'drawer-sliding-systems', 'Telescopic, undermount, and slim tandem box channels.', TRUE, 1),
('Kitchen Accessories', 'Cabinet Hinges', 'cabinet-hinges', 'Clip-on soft close 3D adjustable concealed hinges.', TRUE, 2),
('Kitchen Accessories', 'Flap & Lift-up Fittings', 'flap-lift-up-fittings', 'Hydraulic, gas-lift, and bi-fold lift-up mechanisms.', TRUE, 3),
('Kitchen Accessories', 'Cabinet Hardware & Connectors', 'cabinet-hardware-connectors', 'Minifix, cam locks, shelf studs, and joint fittings.', TRUE, 4),
('Kitchen Accessories', 'Corner & Storage Pull-outs', 'corner-storage-pullouts', 'Magic corner, carousel, and blind corner storage systems.', TRUE, 5),
('Kitchen Accessories', 'Baskets & Organizers', 'baskets-organizers', 'Stainless steel cutlery trays, plate racks, and pantry pullouts.', TRUE, 6),
('Kitchen Accessories', 'Waste Management', 'waste-management', 'Under-sink pull-out dual bins and segregation units.', TRUE, 7),
('Kitchen Accessories', 'Sink & Under-sink Units', 'sink-undersink-units', 'Drip trays, cleaning racks, and detergent pull-outs.', TRUE, 8),
('Kitchen Accessories', 'Shelving & Pull-downs', 'shelving-pulldowns', 'Overhead pull-down pantry shelving and brackets.', TRUE, 9),
('Kitchen Accessories', 'Furniture Supports & Legs', 'furniture-supports-legs', 'Plinth legs, adjustable leveling feet, and table bases.', TRUE, 10),
('Kitchen Accessories', 'Kitchen Utility Rails', 'kitchen-utility-rails', 'Hanging utensil rails, hooks, and spice racks.', TRUE, 11),
('Kitchen Accessories', 'Profile & Handle Solutions', 'profile-handle-solutions', 'Gola profiles, edge profiles, and integrated J-pulls.', TRUE, 12)
ON CONFLICT (category_name, name) DO NOTHING;

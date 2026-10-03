-- V41: Persist optional admin-selected PDF import defaults across asynchronous processing.
ALTER TABLE pdf_import_jobs ADD COLUMN IF NOT EXISTS default_brand_id BIGINT REFERENCES brands(id) ON DELETE SET NULL;
ALTER TABLE pdf_import_jobs ADD COLUMN IF NOT EXISTS default_category_id BIGINT REFERENCES product_categories(id) ON DELETE SET NULL;

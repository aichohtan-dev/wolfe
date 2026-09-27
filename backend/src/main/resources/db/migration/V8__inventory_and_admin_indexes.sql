create index if not exists idx_inventory_updated_at on inventory(updated_at desc);
create index if not exists idx_products_category on products(category);

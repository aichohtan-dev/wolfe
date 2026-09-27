create table if not exists visual_contents (
 id bigserial primary key,
 placement varchar(80) not null,
 title varchar(120) not null,
 subtitle varchar(500),
 media_type varchar(20) not null,
 media_url varchar(1200) not null,
 poster_url varchar(500),
 link_url varchar(500),
 active boolean not null default true,
 sort_order integer not null default 0
);
create index if not exists idx_visual_contents_placement on visual_contents(placement,active,sort_order);
create table if not exists accessory_options (
 id bigserial primary key,
 product_id bigint not null references products(id),
 name varchar(120) not null,
 type varchar(120) not null,
 overlay_url varchar(1200) not null,
 sku varchar(120),
 price numeric(12,2),
 x double precision not null default 50,
 y double precision not null default 50,
 scale double precision not null default 1,
 active boolean not null default true
);
create index if not exists idx_accessory_options_product on accessory_options(product_id,active,type,name);

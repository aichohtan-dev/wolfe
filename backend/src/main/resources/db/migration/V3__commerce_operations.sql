create table if not exists inventory (
  product_id bigint primary key references products(id) on delete cascade,
  quantity integer not null default 0,
  reserved integer not null default 0,
  updated_at timestamptz not null default now(),
  check (quantity >= 0),
  check (reserved >= 0),
  check (reserved <= quantity)
);

create table if not exists orders (
  id varchar(40) primary key,
  customer_id bigint references customers(id),
  status varchar(32) not null,
  total bigint not null,
  currency varchar(3) not null default 'INR',
  created_at timestamptz not null default now()
);

create table if not exists order_items (
  id bigserial primary key,
  order_id varchar(40) not null references orders(id) on delete cascade,
  product_id bigint not null references products(id),
  quantity integer not null,
  unit_price bigint not null,
  check (quantity > 0)
);

create index if not exists idx_orders_customer_created on orders(customer_id, created_at desc);
create index if not exists idx_order_items_order on order_items(order_id);

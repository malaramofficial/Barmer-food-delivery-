-- Payment lifecycle state
alter table public.orders add column if not exists payment_status text not null default 'pending';
alter table public.orders add column if not exists payment_reference text;
alter table public.orders add constraint orders_payment_status_check check(payment_status in ('pending','paid','failed','refunded','cod_pending'));
create index if not exists idx_orders_payment_status on public.orders(payment_status,created_at desc);

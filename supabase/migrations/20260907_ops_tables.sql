-- Operational tables used by payout/support/KYC features.
create table if not exists public.support_tickets(id uuid primary key default gen_random_uuid(),user_id uuid not null references auth.users(id) on delete cascade,category text not null,message text not null,status text not null default 'open',admin_note text,created_at timestamptz not null default now(),updated_at timestamptz not null default now());
create table if not exists public.rider_earnings(id uuid primary key default gen_random_uuid(),rider_id uuid not null references auth.users(id) on delete cascade,order_id uuid not null references public.orders(id) on delete cascade,amount numeric(10,2) not null check(amount>=0),status text not null default 'pending',created_at timestamptz not null default now(),unique(rider_id,order_id));
create table if not exists public.payout_requests(id uuid primary key default gen_random_uuid(),rider_id uuid not null references auth.users(id) on delete cascade,amount numeric(10,2) not null check(amount>0),status text not null default 'requested',created_at timestamptz not null default now());
create table if not exists public.kyc_documents(id uuid primary key default gen_random_uuid(),user_id uuid not null references auth.users(id) on delete cascade,document_type text not null,storage_path text not null,status text not null default 'pending',created_at timestamptz not null default now());

alter table public.support_tickets enable row level security; alter table public.rider_earnings enable row level security; alter table public.payout_requests enable row level security; alter table public.kyc_documents enable row level security;
do $$ begin
 drop policy if exists "users create support tickets" on public.support_tickets; drop policy if exists "users read own support tickets" on public.support_tickets;
 create policy "users create support tickets" on public.support_tickets for insert with check(auth.uid()=user_id);
 create policy "users read own support tickets" on public.support_tickets for select using(auth.uid()=user_id or public.is_admin());
 drop policy if exists "riders read own earnings" on public.rider_earnings; create policy "riders read own earnings" on public.rider_earnings for select using(auth.uid()=rider_id or public.is_admin());
 drop policy if exists "riders create payout requests" on public.payout_requests; drop policy if exists "riders read payout requests" on public.payout_requests;
 create policy "riders create payout requests" on public.payout_requests for insert with check(auth.uid()=rider_id);
 create policy "riders read payout requests" on public.payout_requests for select using(auth.uid()=rider_id or public.is_admin());
 drop policy if exists "users create own kyc" on public.kyc_documents; drop policy if exists "users read own kyc" on public.kyc_documents;
 create policy "users create own kyc" on public.kyc_documents for insert with check(auth.uid()=user_id);
 create policy "users read own kyc" on public.kyc_documents for select using(auth.uid()=user_id or public.is_admin());
exception when undefined_table then null; end $$;
create index if not exists idx_support_user_status on public.support_tickets(user_id,status,created_at desc);create index if not exists idx_earnings_rider_created on public.rider_earnings(rider_id,created_at desc);create index if not exists idx_payout_rider_status on public.payout_requests(rider_id,status,created_at desc);create index if not exists idx_kyc_user_status on public.kyc_documents(user_id,status,created_at desc);

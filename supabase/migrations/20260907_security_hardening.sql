-- Barmer Food Delivery security hardening for Firebase-authenticated users.
create or replace function public.is_admin() returns boolean language sql stable security definer set search_path=public as $$ select exists(select 1 from public.profiles where id=public.current_profile_id() and role='admin'); $$;
revoke all on function public.is_admin() from public; grant execute on function public.is_admin() to authenticated;

-- Prevent clients from mutating approval/ownership/payment-sensitive fields directly.
do $$ begin
  drop policy if exists "admin read restaurant applications" on public.restaurant_applications;
  drop policy if exists "admin read rider applications" on public.rider_applications;
  create policy "admin read restaurant applications" on public.restaurant_applications for select using (public.is_admin() or public.current_profile_id()=applicant_id);
  create policy "admin read rider applications" on public.rider_applications for select using (public.is_admin() or public.current_profile_id()=applicant_id);
exception when undefined_table then null; end $$;

do $$ begin
  drop policy if exists "admins read all orders" on public.orders;
  create policy "admins read all orders" on public.orders for select using (public.is_admin() or public.current_profile_id()=customer_id or public.current_profile_id()=rider_id or exists(select 1 from public.restaurants r where r.id=restaurant_id and r.owner_id=public.current_profile_id()));
exception when undefined_table then null; end $$;

do $$ begin
  drop policy if exists "admins read all notifications" on public.notifications;
  create policy "admins read all notifications" on public.notifications for select using (public.is_admin() or public.current_profile_id()=user_id);
exception when undefined_table then null; end $$;

do $$ begin
  alter table public.rider_locations enable row level security;
  drop policy if exists "riders read own location" on public.rider_locations;
  create policy "riders read own location" on public.rider_locations for select using (public.is_admin() or public.current_profile_id()=rider_id);
exception when undefined_table then null; end $$;

create index if not exists idx_restaurants_approved_open on public.restaurants(is_approved,is_open);
create index if not exists idx_restaurant_apps_status_created on public.restaurant_applications(status,created_at desc);
create index if not exists idx_rider_apps_status_created on public.rider_applications(status,created_at desc);
comment on function public.is_admin() is 'Server-authoritative admin role check. Never replace with client-supplied role.';

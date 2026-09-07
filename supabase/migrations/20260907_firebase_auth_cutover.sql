-- Firebase is the sole application identity provider.
-- Supabase remains the data/API layer and validates Firebase JWTs through Third-Party Auth.
-- This migration removes public-schema foreign-key and trigger dependencies on auth.users.

alter table public.profiles add column if not exists firebase_uid text;
create unique index if not exists idx_profiles_firebase_uid on public.profiles(firebase_uid) where firebase_uid is not null;

-- Remove all public-table foreign keys that point at Supabase Auth users.
do $$
declare r record;
begin
  for r in
    select conrelid::regclass as table_name, conname
    from pg_constraint
    where contype = 'f'
      and confrelid = 'auth.users'::regclass
  loop
    execute format('alter table %s drop constraint if exists %I', r.table_name, r.conname);
  end loop;
end $$;

-- Supabase Auth user-creation trigger is no longer part of the application lifecycle.
drop trigger if exists on_auth_user_created on auth.users;
drop function if exists public.handle_new_user();

create or replace function public.current_profile_id()
returns uuid
language sql
stable
security definer
set search_path = public
as $$
  select p.id
  from public.profiles p
  where p.firebase_uid = (auth.jwt() ->> 'sub')
  limit 1
$$;
revoke all on function public.current_profile_id() from public;
grant execute on function public.current_profile_id() to authenticated;

create or replace function public.ensure_firebase_profile(p_full_name text default 'Customer', p_phone text default '')
returns table(profile_id uuid, role public.app_role)
language plpgsql
security definer
set search_path = public
as $$
declare
  v_uid text := auth.jwt() ->> 'sub';
  v_email text := auth.jwt() ->> 'email';
  v_name text := nullif(trim(coalesce(p_full_name, '')), '');
  v_phone text := nullif(trim(coalesce(p_phone, '')), '');
  v_profile public.profiles;
begin
  if v_uid is null or v_uid = '' then raise exception 'Firebase user identity missing'; end if;
  select * into v_profile from public.profiles where firebase_uid = v_uid limit 1;
  if v_profile.id is null then
    if v_name is null then v_name := split_part(coalesce(v_email, 'Customer'), '@', 1); end if;
    insert into public.profiles (id, firebase_uid, full_name, phone, role)
    values (gen_random_uuid(), v_uid, v_name, v_phone, 'customer')
    returning * into v_profile;
  else
    update public.profiles
      set full_name = coalesce(v_name, full_name),
          phone = coalesce(v_phone, phone),
          updated_at = now()
    where id = v_profile.id
    returning * into v_profile;
  end if;
  return query select v_profile.id, v_profile.role;
end;
$$;
revoke all on function public.ensure_firebase_profile(text,text) from public;
grant execute on function public.ensure_firebase_profile(text,text) to authenticated;

-- Replace Supabase-user-ID RLS checks with the internal Firebase-backed profile ID.
drop policy if exists "users read own profile" on public.profiles;
drop policy if exists "users update own profile" on public.profiles;
create policy "firebase users read own profile" on public.profiles for select using (public.current_profile_id() = id);
create policy "firebase users update own profile" on public.profiles for update using (public.current_profile_id() = id) with check (public.current_profile_id() = id);

drop policy if exists "users create restaurant application" on public.restaurant_applications;
drop policy if exists "users read own restaurant application" on public.restaurant_applications;
drop policy if exists "admin read restaurant applications" on public.restaurant_applications;
create policy "firebase users create restaurant application" on public.restaurant_applications for insert with check (public.current_profile_id() = applicant_id);
create policy "firebase users read restaurant application" on public.restaurant_applications for select using (public.is_admin() or public.current_profile_id() = applicant_id);

drop policy if exists "users create rider application" on public.rider_applications;
drop policy if exists "users read own rider application" on public.rider_applications;
drop policy if exists "admin read rider applications" on public.rider_applications;
create policy "firebase users create rider application" on public.rider_applications for insert with check (public.current_profile_id() = applicant_id);
create policy "firebase users read rider application" on public.rider_applications for select using (public.is_admin() or public.current_profile_id() = applicant_id);

drop policy if exists "customers read own orders" on public.orders;
drop policy if exists "admins read all orders" on public.orders;
create policy "firebase users read own orders" on public.orders for select using (
  public.is_admin() or public.current_profile_id() = customer_id or public.current_profile_id() = rider_id
  or exists(select 1 from public.restaurants r where r.id = restaurant_id and r.owner_id = public.current_profile_id())
);

drop policy if exists "customers read own order items" on public.order_items;
create policy "firebase users read own order items" on public.order_items for select using (
  exists(select 1 from public.orders o where o.id = order_id and (
    o.customer_id = public.current_profile_id() or o.rider_id = public.current_profile_id()
    or exists(select 1 from public.restaurants r where r.id = o.restaurant_id and r.owner_id = public.current_profile_id())
  ))
);

drop policy if exists "users read own notifications" on public.notifications;
drop policy if exists "users update own notifications" on public.notifications;
drop policy if exists "admins read all notifications" on public.notifications;
create policy "firebase users read own notifications" on public.notifications for select using (public.is_admin() or public.current_profile_id() = user_id);
create policy "firebase users update own notifications" on public.notifications for update using (public.current_profile_id() = user_id) with check (public.current_profile_id() = user_id);

drop policy if exists "riders read own rejections" on public.rider_order_rejections;
drop policy if exists "riders create own rejections" on public.rider_order_rejections;
create policy "firebase riders read own rejections" on public.rider_order_rejections for select using (public.current_profile_id() = rider_id);
create policy "firebase riders create own rejections" on public.rider_order_rejections for insert with check (public.current_profile_id() = rider_id);

drop policy if exists "customers read own reviews" on public.reviews;
drop policy if exists "customers create own reviews" on public.reviews;
drop policy if exists "customers review delivered own order" on public.reviews;
create policy "firebase customers read own reviews" on public.reviews for select using (public.current_profile_id() = customer_id);
create policy "firebase customers review delivered order" on public.reviews for insert with check (
  public.current_profile_id() = customer_id
  and exists (
    select 1 from public.orders o
    where o.id = order_id
      and o.customer_id = public.current_profile_id()
      and o.restaurant_id = restaurant_id
      and o.status = 'delivered'
  )
);

create or replace function public.is_admin()
returns boolean
language sql
stable
security definer
set search_path = public
as $$
  select exists(select 1 from public.profiles where id = public.current_profile_id() and role = 'admin')
$$;
revoke all on function public.is_admin() from public;
grant execute on function public.is_admin() to authenticated;

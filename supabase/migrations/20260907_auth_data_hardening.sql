-- Authentication/data-integrity hardening for the native client.
-- Apply after schema.sql and the existing security migrations.

-- Create a profile automatically for every new Auth user.
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  insert into public.profiles (id, full_name, phone, role)
  values (
    new.id,
    coalesce(nullif(trim(new.raw_user_meta_data->>'full_name'), ''), split_part(coalesce(new.email, 'Customer'), '@', 1)),
    nullif(trim(new.raw_user_meta_data->>'phone'), ''),
    'customer'
  )
  on conflict (id) do nothing;
  return new;
end;
$$;

revoke all on function public.handle_new_user() from public;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
after insert on auth.users
for each row execute function public.handle_new_user();

-- A customer must never be able to promote their own profile to admin/restaurant/rider.
create or replace function public.prevent_client_role_escalation()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  if old.role is distinct from new.role and coalesce(auth.role(), '') <> 'service_role' then
    new.role := old.role;
  end if;
  new.id := old.id;
  new.created_at := old.created_at;
  new.updated_at := now();
  return new;
end;
$$;

revoke all on function public.prevent_client_role_escalation() from public;
drop trigger if exists protect_profile_fields on public.profiles;
create trigger protect_profile_fields
before update on public.profiles
for each row execute function public.prevent_client_role_escalation();

-- Orders must be created through the server-side create-order Edge Function,
-- which recalculates menu prices and delivery fees. Remove direct client inserts.
drop policy if exists "customers create own orders" on public.orders;

-- Reviews must refer to a delivered order owned by the submitting customer.
drop policy if exists "customers create own reviews" on public.reviews;
create policy "customers review delivered own order"
on public.reviews for insert
with check (
  auth.uid() = customer_id
  and exists (
    select 1 from public.orders o
    where o.id = order_id
      and o.customer_id = auth.uid()
      and o.restaurant_id = restaurant_id
      and o.status = 'delivered'
  )
);

-- Users may only mark their own notifications read; immutable ownership/data
-- is protected by this trigger rather than trusting the Android client.
create or replace function public.protect_notification_fields()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  if old.user_id <> new.user_id or old.order_id is distinct from new.order_id or old.type <> new.type or old.title <> new.title or old.body <> new.body or old.data <> new.data or old.created_at <> new.created_at then
    new.user_id := old.user_id;
    new.order_id := old.order_id;
    new.type := old.type;
    new.title := old.title;
    new.body := old.body;
    new.data := old.data;
    new.created_at := old.created_at;
  end if;
  return new;
end;
$$;

revoke all on function public.protect_notification_fields() from public;
drop trigger if exists protect_notification_fields on public.notifications;
create trigger protect_notification_fields
before update on public.notifications
for each row execute function public.protect_notification_fields();

-- Useful integrity indexes.
create unique index if not exists idx_profiles_phone_nonempty
on public.profiles(phone)
where phone is not null and length(trim(phone)) > 0;
create index if not exists idx_orders_customer_status_created
on public.orders(customer_id,status,created_at desc);
create index if not exists idx_orders_rider_status_created
on public.orders(rider_id,status,created_at desc);

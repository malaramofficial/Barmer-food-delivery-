-- Data-integrity hardening for the Firebase-authenticated native client.
-- Firebase creates application profiles through ensure_firebase_profile.

create or replace function public.prevent_client_role_escalation()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  if old.role is distinct from new.role and coalesce(auth.role(), '') <> 'service_role' then new.role := old.role; end if;
  new.id := old.id;
  new.firebase_uid := old.firebase_uid;
  new.created_at := old.created_at;
  new.updated_at := now();
  return new;
end;
$$;
revoke all on function public.prevent_client_role_escalation() from public;
drop trigger if exists protect_profile_fields on public.profiles;
create trigger protect_profile_fields before update on public.profiles for each row execute function public.prevent_client_role_escalation();

-- Orders must be created through create-order, which recalculates prices/fees server-side.
drop policy if exists "customers create own orders" on public.orders;

-- Reviews must refer to a delivered order owned by the submitting customer.
drop policy if exists "customers create own reviews" on public.reviews;
drop policy if exists "customers review delivered own order" on public.reviews;
create policy "customers review delivered own order" on public.reviews for insert with check (
  public.current_profile_id() = customer_id
  and exists (
    select 1 from public.orders o
    where o.id = order_id and o.customer_id = public.current_profile_id() and o.restaurant_id = restaurant_id and o.status = 'delivered'
  )
);

-- Notifications can only have read-state changed by the client.
create or replace function public.protect_notification_fields()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  if old.user_id <> new.user_id or old.order_id is distinct from new.order_id or old.type <> new.type or old.title <> new.title or old.body <> new.body or old.data <> new.data or old.created_at <> new.created_at then
    new.user_id := old.user_id; new.order_id := old.order_id; new.type := old.type; new.title := old.title; new.body := old.body; new.data := old.data; new.created_at := old.created_at;
  end if;
  return new;
end;
$$;
revoke all on function public.protect_notification_fields() from public;
drop trigger if exists protect_notification_fields on public.notifications;
create trigger protect_notification_fields before update on public.notifications for each row execute function public.protect_notification_fields();

create index if not exists idx_orders_customer_status_created on public.orders(customer_id,status,created_at desc);
create index if not exists idx_orders_rider_status_created on public.orders(rider_id,status,created_at desc);

-- Lightweight immutable operational audit trail for security-sensitive actions.
create table if not exists public.audit_logs(
  id uuid primary key default gen_random_uuid(),
  actor_id uuid references auth.users(id) on delete set null,
  action text not null,
  entity_type text not null,
  entity_id uuid,
  metadata jsonb not null default '{}'::jsonb,
  created_at timestamptz not null default now()
);
alter table public.audit_logs enable row level security;
drop policy if exists "admins read audit logs" on public.audit_logs;
create policy "admins read audit logs" on public.audit_logs for select using(public.is_admin());
create index if not exists idx_audit_created on public.audit_logs(created_at desc);
create index if not exists idx_audit_entity on public.audit_logs(entity_type,entity_id,created_at desc);
comment on table public.audit_logs is 'Security/operations audit events. Do not store raw KYC document contents or sensitive ID numbers here.';

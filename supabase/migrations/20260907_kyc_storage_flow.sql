-- Barmer Food Delivery — production KYC storage foundation.
-- Run after the existing operational tables/security migrations.

create table if not exists public.kyc_documents(
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  document_type text not null,
  application_type text,
  application_id uuid,
  storage_path text not null,
  original_name text,
  mime_type text,
  size_bytes bigint,
  status text not null default 'pending' check(status in ('pending','approved','rejected')),
  admin_note text,
  reviewed_by uuid references auth.users(id) on delete set null,
  reviewed_at timestamptz,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

alter table public.kyc_documents add column if not exists application_type text;
alter table public.kyc_documents add column if not exists application_id uuid;
alter table public.kyc_documents add column if not exists original_name text;
alter table public.kyc_documents add column if not exists mime_type text;
alter table public.kyc_documents add column if not exists size_bytes bigint;
alter table public.kyc_documents add column if not exists admin_note text;
alter table public.kyc_documents add column if not exists reviewed_by uuid references auth.users(id) on delete set null;
alter table public.kyc_documents add column if not exists reviewed_at timestamptz;
alter table public.kyc_documents add column if not exists updated_at timestamptz not null default now();

create index if not exists idx_kyc_status_created on public.kyc_documents(status,created_at desc);
create index if not exists idx_kyc_application on public.kyc_documents(application_type,application_id,created_at desc);
create unique index if not exists uq_kyc_document_submission on public.kyc_documents(user_id,document_type,coalesce(application_type,''),coalesce(application_id,'00000000-0000-0000-0000-000000000000'::uuid));

insert into storage.buckets(id,name,public,file_size_limit,allowed_mime_types)
values('kyc-documents','kyc-documents',false,5242880,array['image/jpeg','image/png','application/pdf'])
on conflict(id) do update set public=false,file_size_limit=5242880,allowed_mime_types=array['image/jpeg','image/png','application/pdf'];

alter table public.kyc_documents enable row level security;
do $$ begin
  drop policy if exists "kyc users insert own" on public.kyc_documents;
  drop policy if exists "kyc users read own" on public.kyc_documents;
  create policy "kyc users insert own" on public.kyc_documents for insert with check(auth.uid()=user_id);
  create policy "kyc users read own" on public.kyc_documents for select using(auth.uid()=user_id or public.is_admin());
exception when undefined_function then
  null;
end $$;

-- Private bucket: a user's folder is the first path segment and must equal auth.uid().
drop policy if exists "kyc storage insert own folder" on storage.objects;
drop policy if exists "kyc storage read own or admin" on storage.objects;
drop policy if exists "kyc storage update own pending" on storage.objects;
create policy "kyc storage insert own folder" on storage.objects
for insert to authenticated
with check(bucket_id='kyc-documents' and (storage.foldername(name))[1]=auth.uid()::text);
create policy "kyc storage read own or admin" on storage.objects
for select to authenticated
using(bucket_id='kyc-documents' and ((storage.foldername(name))[1]=auth.uid()::text or public.is_admin()));
create policy "kyc storage update own pending" on storage.objects
for update to authenticated
using(bucket_id='kyc-documents' and (storage.foldername(name))[1]=auth.uid()::text)
with check(bucket_id='kyc-documents' and (storage.foldername(name))[1]=auth.uid()::text);

comment on table public.kyc_documents is 'Private KYC metadata; raw documents live only in the private kyc-documents Storage bucket.';
comment on column public.kyc_documents.storage_path is 'Private bucket object path. Never expose as a public URL.';

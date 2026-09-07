do $$ begin
  if to_regclass('public.kcy_documents') is not null and to_regclass('public.kyc_documents') is null then
    alter table public.kcy_documents rename to kyc_documents;
  end if;
end $$;
create table if not exists public.kyc_documents(id uuid primary key default gen_random_uuid(),user_id uuid not null references auth.users(id) on delete cascade,application_type text not null check(application_type in ('restaurant','rider')),document_type text not null,storage_path text not null,status text not null default 'pending' check(status in ('pending','approved','rejected')),admin_note text,created_at timestamptz not null default now(),reviewed_at timestamptz,unique(user_id,application_type,document_type));
alter table public.kyc_documents enable row level security;

-- KYC security smoke checklist. Run in Supabase SQL editor with a test project.
-- These are structural checks; never use real identity documents in test data.
select id,name,public,file_size_limit,allowed_mime_types from storage.buckets where id='kyc-documents';
select relname,relrowsecurity from pg_class where oid='public.kyc_documents'::regclass;
select policyname,cmd,roles from pg_policies where schemaname='storage' and tablename='objects' and policyname like 'kyc storage%';
select policyname,cmd,roles from pg_policies where schemaname='public' and tablename='kyc_documents';
select column_name,data_type from information_schema.columns where table_schema='public' and table_name='kyc_documents' order by ordinal_position;
-- Manual checks:
-- 1) Anonymous upload must fail.
-- 2) User A cannot upload to user B's folder.
-- 3) User A cannot select user B's object.
-- 4) Non-admin cannot generate a signed URL for user B.
-- 5) Admin can generate a 5-minute signed URL.
-- 6) KYC submit rejects missing/deleted objects.
-- 7) KYC submit rejects paths outside the authenticated user's folder.
-- 8) Files above 5 MB or unsupported MIME types are rejected.
-- 9) Duplicate document submission returns conflict.
-- 10) Admin review is the only route to approved/rejected status.

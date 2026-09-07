-- Security smoke tests. Run in Supabase SQL editor with an authenticated test user/session.
-- Verify RLS is enabled on all application tables:
select n.nspname as schema_name,c.relname as table_name,c.relrowsecurity as rls_enabled from pg_class c join pg_namespace n on n.oid=c.relnamespace where n.nspname='public' and c.relkind='r' order by c.relname;

-- Verify admin helper is security definer and has a restricted search_path.
select p.proname,p.prosecdef,pg_get_functiondef(p.oid) from pg_proc p join pg_namespace n on n.oid=p.pronamespace where n.nspname='public' and p.proname='is_admin';

-- Verify no service-role secret is accidentally stored in client configuration.
-- This is a source-review checklist item: config.js must contain only public anon credentials.

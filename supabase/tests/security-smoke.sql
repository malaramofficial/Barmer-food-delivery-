-- Security smoke tests. Run in Supabase SQL editor with an authenticated test user/session.

-- 1) Every exposed application table must have RLS enabled.
select n.nspname as schema_name,c.relname as table_name,c.relrowsecurity as rls_enabled
from pg_class c join pg_namespace n on n.oid=c.relnamespace
where n.nspname='public' and c.relkind='r'
order by c.relname;

-- 2) Admin helper and auth profile trigger must be security-definer and pinned.
select p.proname,p.prosecdef,pg_get_functiondef(p.oid)
from pg_proc p join pg_namespace n on n.oid=p.pronamespace
where n.nspname='public' and p.proname in ('is_admin','handle_new_user','prevent_client_role_escalation');

-- 3) Verify signup trigger exists.
select tgname,tgenabled
from pg_trigger
where tgrelid='auth.users'::regclass and tgname='on_auth_user_created';

-- 4) Direct customer order creation must NOT have an INSERT policy.
select polname,polcmd,pg_get_expr(polqual,polrelid) as using_expr,pg_get_expr(polwithcheck,polrelid) as check_expr
from pg_policy
where polrelid='public.orders'::regclass;

-- 5) Review creation must require a delivered own order.
select polname,polcmd,pg_get_expr(polwithcheck,polrelid) as check_expr
from pg_policy
where polrelid='public.reviews'::regclass and polcmd='a';

-- 6) Client configuration must contain only public publishable/anon credentials.
-- Never commit service-role or secret keys to Android/web source.

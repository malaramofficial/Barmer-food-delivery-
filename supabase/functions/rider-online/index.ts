import { createClient } from 'https://esm.sh/@supabase/supabase-js@2';

const cors = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
};
const json = (b: unknown, s = 200) => new Response(JSON.stringify(b), { status: s, headers: { ...cors, 'Content-Type': 'application/json' } });

Deno.serve(async (req) => {
  if (req.method === 'OPTIONS') return new Response('ok', { headers: cors });
  if (req.method !== 'POST') return json({ error: 'Method not allowed' }, 405);

  const service = createClient(Deno.env.get('SUPABASE_URL')!, Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')!);
  const auth = createClient(Deno.env.get('SUPABASE_URL')!, Deno.env.get('SUPABASE_ANON_KEY')!, {
    global: { headers: { Authorization: req.headers.get('Authorization') ?? '' } },
  });
  const { data: { user } } = await auth.auth.getUser();
  if (!user) return json({ error: 'Authentication required' }, 401);

  const { data: profile } = await service.from('profiles').select('role').eq('id', user.id).single();
  if (profile?.role !== 'rider') return json({ error: 'Approved rider only' }, 403);

  const body = await req.json().catch(() => null);
  if (typeof body?.is_online !== 'boolean') return json({ error: 'is_online must be boolean' }, 400);

  const { data: application } = await service.from('rider_applications')
    .select('status').eq('applicant_id', user.id).eq('status', 'approved').limit(1).maybeSingle();
  if (!application) return json({ error: 'Rider approval required' }, 403);

  const { data, error } = await service.from('rider_locations').upsert({
    rider_id: user.id,
    is_online: body.is_online,
    latitude: Number(body.latitude) || 25.75,
    longitude: Number(body.longitude) || 71.39,
    accuracy_m: Number.isFinite(Number(body.accuracy_m)) ? Number(body.accuracy_m) : null,
    heading: Number.isFinite(Number(body.heading)) ? Number(body.heading) : null,
    updated_at: new Date().toISOString(),
  }).select().single();
  if (error) return json({ error: 'Could not update rider availability' }, 500);

  return json({ ok: true, is_online: data.is_online });
});

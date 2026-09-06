import { createClient } from 'https://esm.sh/@supabase/supabase-js@2';

const cors = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
};
const json = (b: unknown, s = 200) =>
  new Response(JSON.stringify(b), { status: s, headers: { ...cors, 'Content-Type': 'application/json' } });

Deno.serve(async (req) => {
  if (req.method === 'OPTIONS') return new Response('ok', { headers: cors });
  if (req.method !== 'POST') return json({ error: 'Method not allowed' }, 405);

  const service = createClient(Deno.env.get('SUPABASE_URL')!, Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')!);
  const auth = createClient(Deno.env.get('SUPABASE_URL')!, Deno.env.get('SUPABASE_ANON_KEY')!, {
    global: { headers: { Authorization: req.headers.get('Authorization') ?? '' } },
  });
  const { data: { user } } = await auth.auth.getUser();
  if (!user) return json({ error: 'Authentication required' }, 401);

  const { data: me } = await service.from('profiles').select('role').eq('id', user.id).single();
  if (me?.role !== 'admin') return json({ error: 'Admin only' }, 403);

  const body = await req.json().catch(() => null);
  if (!body?.type || !body?.application_id || !['restaurant', 'rider'].includes(body.type) ||
      !['approved', 'rejected', 'suspended'].includes(body.status)) {
    return json({ error: 'Invalid review request' }, 400);
  }

  const table = body.type === 'restaurant' ? 'restaurant_applications' : 'rider_applications';
  const { data: application, error } = await service.from(table)
    .update({
      status: body.status,
      reviewed_by: user.id,
      reviewed_at: new Date().toISOString(),
      admin_note: typeof body.note === 'string' ? body.note.slice(0, 1000) : null,
    })
    .eq('id', body.application_id)
    .eq('status', 'pending')
    .select()
    .single();
  if (error || !application) return json({ error: error?.message ?? 'Application not found or already reviewed' }, 409);

  if (body.status === 'approved') {
    const applicantId = application.applicant_id;
    const role = body.type === 'restaurant' ? 'restaurant' : 'rider';
    await service.from('profiles').update({ role, full_name: body.type === 'restaurant' ? application.owner_name : application.full_name, phone: application.phone }).eq('id', applicantId);

    if (body.type === 'restaurant') {
      const { error: restaurantError } = await service.from('restaurants').upsert({
        owner_id: applicantId,
        name: application.restaurant_name,
        address: application.address,
        area: application.barmer_area,
        phone: application.phone,
        is_approved: true,
        is_open: false,
      }, { onConflict: 'owner_id' });
      if (restaurantError) return json({ error: 'Application approved but restaurant provisioning failed' }, 500);
    }
  }

  if (body.status === 'rejected' || body.status === 'suspended') {
    await service.from('profiles').update({ role: 'customer' }).eq('id', application.applicant_id).in('role', ['restaurant', 'rider']);
    if (body.type === 'restaurant') {
      await service.from('restaurants').update({ is_approved: false, is_open: false }).eq('owner_id', application.applicant_id);
    }
  }

  await service.from('notifications').insert({
    user_id: application.applicant_id,
    type: 'application',
    title: body.status === 'approved' ? 'आवेदन स्वीकृत' : 'आवेदन की स्थिति अपडेट',
    body: body.status === 'approved' ? 'आपका आवेदन स्वीकृत हो गया है।' : `आपका आवेदन ${body.status} किया गया है।`,
    data: { application_id: application.id, status: body.status, type: body.type },
  });

  return json({ ok: true, status: body.status, application_id: application.id });
});

import { createClient } from 'https://esm.sh/@supabase/supabase-js@2';

const MAX_BYTES = 5 * 1024 * 1024;
const ALLOWED = new Set(['image/jpeg','image/png','application/pdf']);
const TYPES = new Set(['aadhaar','pan','driving_license','vehicle_rc','bank_proof','profile_photo','other']);
function json(data: unknown, status = 200) { return Response.json(data, { status, headers: { 'Cache-Control': 'no-store' } }); }

Deno.serve(async req => {
  if (req.method !== 'POST') return new Response('Method Not Allowed', { status: 405 });
  const token = (req.headers.get('Authorization') || '').replace(/^Bearer\s+/i, '');
  const base = Deno.env.get('SUPABASE_URL'), anonKey = Deno.env.get('SUPABASE_ANON_KEY'), serviceKey = Deno.env.get('SUPABASE_SERVICE_ROLE_KEY');
  if (!base || !anonKey || !serviceKey) return json({ error: 'Server configuration incomplete' }, 500);
  const anon = createClient(base, anonKey), sb = createClient(base, serviceKey);
  const { data: { user } } = await anon.auth.getUser(token); if (!user) return json({ error: 'Unauthorized' }, 401);
  const b = await req.json().catch(() => null);
  const type = String(b?.document_type || '').trim().toLowerCase(), path = String(b?.storage_path || '').trim();
  const applicationType = String(b?.application_type || '').trim().toLowerCase() || null;
  const applicationId = b?.application_id ? String(b.application_id) : null;
  const originalName = String(b?.original_name || '').trim().slice(0, 180) || null;
  if (!TYPES.has(type) || !path || path.length > 500 || !path.startsWith(`${user.id}/`)) return json({ error: 'Invalid KYC payload' }, 400);
  if (applicationType && !['restaurant','rider'].includes(applicationType)) return json({ error: 'Invalid application type' }, 400);
  if (applicationId && !/^[0-9a-f-]{36}$/i.test(applicationId)) return json({ error: 'Invalid application id' }, 400);
  if (applicationId) {
    const table = applicationType === 'restaurant' ? 'restaurant_applications' : applicationType === 'rider' ? 'rider_applications' : null;
    if (!table) return json({ error: 'Application type is required when application id is supplied' }, 400);
    const q = await sb.from(table).select('id').eq('id', applicationId).eq('applicant_id', user.id).maybeSingle();
    if (q.error) return json({ error: q.error.message }, 500);
    if (!q.data) return json({ error: 'Application not found for this account' }, 403);
  }
  const { data: object, error: objectError } = await sb.storage.from('kyc-documents').list(user.id, { limit: 100 });
  if (objectError) return json({ error: 'Unable to verify uploaded document' }, 500);
  const leaf = path.slice(user.id.length + 1), meta = (object || []).find((x: any) => x.name === leaf);
  if (!meta) return json({ error: 'Uploaded file not found' }, 400);
  const mime = String(meta.metadata?.mimetype || meta.metadata?.mimeType || '').toLowerCase(), size = Number(meta.metadata?.size || 0);
  if (mime && !ALLOWED.has(mime)) return json({ error: 'Unsupported document type' }, 400);
  if (size > MAX_BYTES) return json({ error: 'Document exceeds 5 MB limit' }, 400);
  const { data, error } = await sb.from('kyc_documents').insert({user_id:user.id,document_type:type,application_type:applicationType,application_id:applicationId,storage_path:path,original_name:originalName,mime_type:mime||null,size_bytes:size||null,status:'pending'}).select('id,status,document_type,created_at').single();
  if (error) return json({ error: error.message }, error.code === '23505' ? 409 : 500);
  return json(data);
});

import { createClient } from 'https://esm.sh/@supabase/supabase-js@2';
import { isInBarmer, geofenceInfo } from '../_shared/geofence.ts';

const cors = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
};
const json = (body: unknown, status = 200) => new Response(JSON.stringify(body), { status, headers: { ...cors, 'Content-Type': 'application/json' } });

Deno.serve(async (req) => {
  if (req.method === 'OPTIONS') return new Response('ok', { headers: cors });
  if (req.method !== 'POST') return json({ error: 'Method not allowed' }, 405);

  // Supabase is only validating the Firebase JWT here; Firebase remains the identity provider.
  const auth = createClient(Deno.env.get('SUPABASE_URL')!, Deno.env.get('SUPABASE_ANON_KEY')!, { global: { headers: { Authorization: req.headers.get('Authorization') ?? '' } } });
  const { data: { user } } = await auth.auth.getUser();
  if (!user) return json({ error: 'Authentication required' }, 401);

  const service = createClient(Deno.env.get('SUPABASE_URL')!, Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')!);
  const { data: profile } = await service.from('profiles').select('id,role').eq('firebase_uid', user.id).maybeSingle();
  if (!profile?.id) return json({ error: 'Firebase profile is not initialized' }, 409);
  const customerId = profile.id;

  const body = await req.json().catch(() => null);
  if (!body?.restaurant_id || !Array.isArray(body.items) || !body.items.length || !body.address) return json({ error: 'restaurant_id, items and address are required' }, 400);
  if (body.items.length > 50 || body.address.length > 500) return json({ error: 'Invalid order size' }, 400);
  const lat = body.latitude == null ? null : Number(body.latitude), lng = body.longitude == null ? null : Number(body.longitude);
  if ((lat == null) !== (lng == null)) return json({ error: 'latitude and longitude must be supplied together' }, 400);
  if (lat != null && (!Number.isFinite(lat) || !Number.isFinite(lng) || !isInBarmer(lat, lng))) return json({ error: 'Delivery location is outside the Barmer service area', area: geofenceInfo(lat, lng) }, 422);
  const ids = body.items.map((x: any) => x?.menu_item_id).filter(Boolean);
  if (!ids.length || ids.length !== new Set(ids).size) return json({ error: 'Invalid or duplicate items' }, 400);
  const { data: restaurant } = await service.from('restaurants').select('id,name,delivery_fee,is_open,is_approved,latitude,longitude').eq('id', body.restaurant_id).eq('is_approved', true).maybeSingle();
  if (!restaurant || !restaurant.is_open) return json({ error: 'Restaurant is unavailable' }, 409);
  if (!Number.isFinite(Number(restaurant.latitude)) || !Number.isFinite(Number(restaurant.longitude)) || !isInBarmer(Number(restaurant.latitude), Number(restaurant.longitude))) return json({ error: 'Restaurant is outside the Barmer service area' }, 409);
  const { data: menu, error: menuError } = await service.from('menu_items').select('id,name,price,is_available,restaurant_id').in('id', ids).eq('restaurant_id', restaurant.id).eq('is_available', true);
  if (menuError || !menu || menu.length !== ids.length) return json({ error: 'One or more menu items are unavailable' }, 409);
  const byId = new Map(menu.map((x: any) => [x.id, x])); let subtotal = 0;
  const orderItems = body.items.map((x: any) => { const item = byId.get(x.menu_item_id); const qty = Math.max(1, Math.min(20, Math.floor(Number(x.quantity) || 1))); subtotal += Number(item.price) * qty; return { menu_item_id: item.id, item_name: item.name, unit_price: item.price, quantity: qty }; });
  const deliveryFee = Math.max(0, Number(restaurant.delivery_fee || 0)); const total = subtotal + deliveryFee; const paymentMethod = body.payment_method === 'online' ? 'online' : 'cod';
  const { data: order, error } = await service.from('orders').insert({ customer_id: customerId, restaurant_id: restaurant.id, delivery_address: body.address, delivery_latitude: lat, delivery_longitude: lng, subtotal, delivery_fee: deliveryFee, total, payment_method: paymentMethod, status: 'restaurant_notified' }).select().single();
  if (error) return json({ error: 'Could not create order' }, 400);
  const { error: itemError } = await service.from('order_items').insert(orderItems.map((x: any) => ({ ...x, order_id: order.id })));
  if (itemError) { await service.from('orders').delete().eq('id', order.id); return json({ error: 'Could not save order items' }, 500); }
  const { data: restaurantOwner } = await service.from('restaurants').select('owner_id').eq('id', restaurant.id).maybeSingle();
  if (restaurantOwner?.owner_id) await service.from('notifications').insert({ user_id: restaurantOwner.owner_id, order_id: order.id, type: 'order', title: 'नया ऑर्डर', body: 'आपके restaurant पर नया order आया है।', data: { status: 'restaurant_notified' } });
  return json({ order_id: order.id, status: order.status, total: order.total });
});

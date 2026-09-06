import { createClient } from 'https://esm.sh/@supabase/supabase-js@2';

const cors = { 'Access-Control-Allow-Origin': '*', 'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type' };
const json = (body: unknown, status = 200) => new Response(JSON.stringify(body), { status, headers: { ...cors, 'Content-Type': 'application/json' } });

Deno.serve(async (req) => {
  if (req.method === 'OPTIONS') return new Response('ok', { headers: cors });
  if (req.method !== 'POST') return json({ error: 'Method not allowed' }, 405);

  const supabase = createClient(Deno.env.get('SUPABASE_URL')!, Deno.env.get('SUPABASE_ANON_KEY')!, { global: { headers: { Authorization: req.headers.get('Authorization') ?? '' } } });
  const { data: { user } } = await supabase.auth.getUser();
  if (!user) return json({ error: 'Authentication required' }, 401);

  const body = await req.json().catch(() => null);
  if (!body?.restaurant_id || !Array.isArray(body.items) || !body.items.length || !body.address) return json({ error: 'restaurant_id, items and address are required' }, 400);

  const ids = body.items.map((x: { menu_item_id?: string }) => x.menu_item_id).filter(Boolean);
  if (!ids.length || ids.length > 50) return json({ error: 'Invalid items' }, 400);

  const { data: restaurant } = await supabase.from('restaurants').select('id,name,delivery_fee,is_open,is_approved').eq('id', body.restaurant_id).eq('is_approved', true).maybeSingle();
  if (!restaurant || !restaurant.is_open) return json({ error: 'Restaurant is unavailable' }, 409);

  const { data: menu, error: menuError } = await supabase.from('menu_items').select('id,name,price,is_available,restaurant_id').in('id', ids).eq('restaurant_id', restaurant.id).eq('is_available', true);
  if (menuError || !menu || menu.length !== ids.length) return json({ error: 'One or more menu items are unavailable' }, 409);

  const byId = new Map(menu.map((x: any) => [x.id, x]));
  let subtotal = 0;
  const orderItems = body.items.map((x: any) => {
    const item = byId.get(x.menu_item_id);
    const qty = Math.max(1, Math.min(20, Number(x.quantity) || 1));
    subtotal += Number(item.price) * qty;
    return { menu_item_id: item.id, name: item.name, unit_price: item.price, quantity: qty };
  });
  const deliveryFee = Number(restaurant.delivery_fee || 0);
  const total = subtotal + deliveryFee;

  const { data: order, error } = await supabase.from('orders').insert({ customer_id: user.id, restaurant_id: restaurant.id, delivery_address: body.address, subtotal, delivery_fee: deliveryFee, total, payment_method: body.payment_method === 'online' ? 'online' : 'cod', status: 'placed' }).select().single();
  if (error) return json({ error: error.message }, 400);

  const { error: itemError } = await supabase.from('order_items').insert(orderItems.map((x: any) => ({ ...x, order_id: order.id })));
  if (itemError) {
    await supabase.from('orders').delete().eq('id', order.id).eq('customer_id', user.id);
    return json({ error: 'Could not save order items' }, 500);
  }

  return json({ order_id: order.id, status: 'placed', total });
});

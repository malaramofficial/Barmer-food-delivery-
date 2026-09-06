/* Barmer Food Delivery — browser Supabase bridge.
 * Safe to load even when production credentials are not configured.
 * Never put a service-role key in this file.
 */
(function () {
  const cfg = window.BFD_CONFIG || {};
  const ready = Boolean(cfg.supabaseUrl && cfg.supabaseAnonKey && window.supabase);
  const client = ready ? window.supabase.createClient(cfg.supabaseUrl, cfg.supabaseAnonKey) : null;

  async function session() {
    if (!client) return null;
    const { data } = await client.auth.getSession();
    return data?.session || null;
  }

  async function user() {
    const s = await session();
    return s?.user || null;
  }

  async function approvedRestaurants() {
    if (!client) return { data: [], error: new Error('Supabase is not configured') };
    return client.from('restaurants')
      .select('id,name,address,latitude,longitude,is_open,delivery_fee,rating')
      .eq('is_approved', true)
      .order('name');
  }

  async function restaurantMenu(restaurantId) {
    if (!client) return { data: [], error: new Error('Supabase is not configured') };
    return client.from('menu_items')
      .select('id,restaurant_id,category_id,name,description,price,is_available,image_url')
      .eq('restaurant_id', restaurantId)
      .eq('is_available', true)
      .order('name');
  }

  async function invoke(name, body) {
    if (!client) throw new Error('Supabase is not configured');
    const { data, error } = await client.functions.invoke(name, { body });
    if (error) throw error;
    return data;
  }

  async function createOrder(payload) { return invoke('create-order', payload); }
  async function orderAction(payload) { return invoke('order-action', payload); }
  async function getTracking(orderId) { return invoke('tracking', { order_id: orderId }); }
  async function notifyOrder(orderId) { return invoke('notify-order', { order_id: orderId }); }

  function subscribeToOrder(orderId, callback) {
    if (!client) return () => {};
    const channel = client.channel('bfd-order-' + orderId)
      .on('postgres_changes', {
        event: 'UPDATE', schema: 'public', table: 'orders', filter: 'id=eq.' + orderId
      }, callback)
      .subscribe();
    return () => { client.removeChannel(channel); };
  }

  function subscribeToNotifications(userId, callback) {
    if (!client || !userId) return () => {};
    const channel = client.channel('bfd-notifications-' + userId)
      .on('postgres_changes', {
        event: 'INSERT', schema: 'public', table: 'notifications', filter: 'user_id=eq.' + userId
      }, callback)
      .subscribe();
    return () => { client.removeChannel(channel); };
  }

  window.BFD_SUPABASE = {
    ready,
    client,
    session,
    user,
    approvedRestaurants,
    restaurantMenu,
    createOrder,
    orderAction,
    getTracking,
    notifyOrder,
    subscribeToOrder,
    subscribeToNotifications
  };
})();

/* Barmer Food Delivery — production customer runtime layer.
 * Loaded after app.js. It leaves the existing demo untouched when Supabase is not configured.
 */
(function () {
  'use strict';
  const api = window.BFD_SUPABASE;
  if (!api?.ready) return;

  const root = document.getElementById('app');
  if (!root) return;

  let restaurants = [];
  let currentRestaurant = null;
  let menu = [];
  let cart = [];
  let orders = [];
  let selectedOrder = null;
  let cleanupOrder = null;
  let loading = false;
  let errorText = '';

  const esc = (v) => String(v ?? '').replace(/[&<>'"]/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'}[c]));
  const money = (v) => `₹${Number(v || 0).toFixed(0)}`;
  const app = () => document.getElementById('app');
  const toast = (text) => { const old = document.querySelector('.bfd-live-toast'); if (old) old.remove(); const el=document.createElement('div'); el.className='toast bfd-live-toast'; el.textContent=text; document.body.appendChild(el); setTimeout(()=>el.remove(),2400); };
  const statusLabel = s => ({placed:'Order placed',restaurant_notified:'Hotel को notification',accepted:'Hotel ने accept किया',preparing:'खाना तैयार हो रहा है',ready_for_pickup:'Pickup के लिए तैयार',rider_assigned:'Rider मिल गया',picked_up:'Rider ने खाना ले लिया',on_the_way:'आपके पास आ रहा है',delivered:'Delivered',cancelled:'Cancelled'}[s] || s);

  function frame(body, title='Barmer Food Delivery') {
    return `<div class="app"><header class="top"><div class="brand"><div><div class="logo">🍽️ ${title}</div><div class="location">📍 Barmer, Rajasthan • केवल Barmer में</div></div><button class="btn secondary" onclick="bfdLiveHome()">⌂</button></div></header><main class="page">${body}</main><nav class="bottom"><div class="nav"><button onclick="bfdLiveHome()">⌂<br><small>Home</small></button><button onclick="bfdLiveSearch()">⌕<br><small>Search</small></button><button onclick="bfdLiveOrders()">🧾<br><small>Orders</small></button><button onclick="bfdLiveProfile()">☻<br><small>Profile</small></button></div></nav></div>`;
  }

  function renderHome() {
    const cards = restaurants.map(r => `<article class="card"><div class="food-img">🍛</div><div class="card-body"><h3>${esc(r.name)}</h3><div class="muted">${esc(r.cuisine || 'Food & Restaurant')}</div><p><span class="pill">★ ${esc(r.rating || 0)}</span> <span class="muted">${r.is_open ? 'Open' : 'Closed'}</span></p><div class="row"><span class="muted">Delivery ${money(r.delivery_fee)}</span><button class="btn primary" onclick="bfdLiveRestaurant('${r.id}')">Menu</button></div></div></article>`).join('');
    root.innerHTML = frame(`<div class="container"><section class="hero"><h1>खाना चाहिए? Barmer में मंगाइए! 😋</h1><p>Verified restaurants से ताज़ा खाना, आपके दरवाज़े तक।</p><div class="search" onclick="bfdLiveSearch()">🔎 होटल, खाना या cuisine खोजें...</div></section><div class="section-title"><h2>Verified Hotels & Restaurants</h2><span class="muted">${restaurants.length} available</span></div>${errorText ? `<div class="panel"><b>⚠️ ${esc(errorText)}</b></div>` : ''}<div class="grid">${cards || '<div class="empty"><h3>अभी कोई approved restaurant नहीं मिला</h3><p>Admin approval के बाद restaurant यहाँ दिखाई देगा।</p></div>'}</div></div>`);
  }

  async function loadHome() {
    loading = true; errorText=''; renderHome();
    const result = await api.approvedRestaurants();
    loading = false;
    if (result.error) errorText=result.error.message || 'Restaurants load नहीं हो सके'; else restaurants=result.data || [];
    renderHome();
  }

  async function openRestaurant(id) {
    currentRestaurant = restaurants.find(r=>r.id===id); if(!currentRestaurant) return;
    root.innerHTML=frame(`<div class="container"><button class="back" onclick="bfdLiveHome()">← वापस</button><section class="hero" style="margin-top:14px"><h1>🍛 ${esc(currentRestaurant.name)}</h1><p>★ ${esc(currentRestaurant.rating || 0)} • ${esc(currentRestaurant.cuisine || 'Food')} • ${esc(currentRestaurant.address || 'Barmer')}</p></section><div id="bfd-menu" class="panel">Menu load हो रहा है…</div></div>`);
    const result=await api.restaurantMenu(id);
    menu=result.data || [];
    if(result.error){ document.getElementById('bfd-menu').innerHTML=`<p>⚠️ ${esc(result.error.message)}</p>`; return; }
    renderMenu();
  }

  function renderMenu() {
    const el=document.getElementById('bfd-menu'); if(!el) return;
    const items=menu.map(i=>`<div class="menu-item"><div class="thumb">🍽️</div><div class="grow"><h3>${esc(i.name)}</h3><p class="muted">${esc(i.description || 'Freshly prepared')}</p><b>${money(i.price)}</b></div><button class="add" onclick="bfdLiveAdd('${i.id}')">+ Add</button></div>`).join('');
    el.innerHTML=`<h3>Menu</h3>${items || '<p class="muted">इस restaurant का menu अभी उपलब्ध नहीं है।</p>'}${cart.length?`<hr><div class="row"><b>🛒 ${cart.length} items</b><b>${money(cart.reduce((s,x)=>s+x.price*x.quantity,0))}</b><button class="btn primary" onclick="bfdLiveCheckout()">Cart →</button></div>`:''}`;
  }

  function add(id){const item=menu.find(x=>x.id===id);if(!item)return;const old=cart.find(x=>x.id===id);if(old)old.quantity++;else cart.push({id:item.id,name:item.name,price:Number(item.price),quantity:1});renderMenu();toast(`${item.name} cart में जुड़ गया`);}

  function checkout() {
    if(!cart.length)return;
    const subtotal=cart.reduce((s,x)=>s+x.price*x.quantity,0), fee=Number(currentRestaurant.delivery_fee||0), total=subtotal+fee;
    root.innerHTML=frame(`<div class="container"><button class="back" onclick="bfdLiveRestaurant('${currentRestaurant.id}')">← Menu</button><h2>Checkout</h2><div class="panel"><h3>Delivery Address</h3><input id="bfd-address" class="search" style="width:100%;border:1px solid #ddd" required placeholder="Barmer delivery address लिखें"><p class="muted">Online payment अभी configured नहीं है; COD production-safe default है।</p></div><div class="panel"><div class="row"><span>Items</span><b>${money(subtotal)}</b></div><div class="row"><span>Delivery</span><b>${money(fee)}</b></div><hr><div class="row"><b>Total</b><b>${money(total)}</b></div><button class="btn primary" style="width:100%;margin-top:14px" onclick="bfdLivePlaceOrder()">Place Order • ${money(total)}</button></div></div>`);
  }

  async function placeOrder() {
    const address=(document.getElementById('bfd-address')?.value||'').trim();
    if(address.length<5){toast('पूरा delivery address लिखें');return;}
    const user=await api.user(); if(!user){toast('Order करने के लिए पहले login करें'); return;}
    loading=true; toast('Order सुरक्षित रूप से बनाया जा रहा है…');
    try {
      const data=await api.createOrder({restaurant_id:currentRestaurant.id,delivery_address:address,delivery_latitude:null,delivery_longitude:null,payment_method:'cod',items:cart.map(x=>({menu_item_id:x.id,quantity:x.quantity}))});
      const orderId=data?.order?.id || data?.id;
      cart=[];
      await loadOrders();
      if(orderId){ selectedOrder=orders.find(o=>o.id===orderId)||{id:orderId,status:data?.order?.status||'placed',restaurant_id:currentRestaurant.id}; await showTracking(selectedOrder.id); }
      else bfdLiveOrders();
    } catch(e){toast(e?.message||'Order create नहीं हुआ');}
    finally{loading=false;}
  }

  async function loadOrders(){const user=await api.user();if(!user){orders=[];return;}const {data,error}=await api.client.from('orders').select('id,restaurant_id,status,delivery_address,total,created_at,rider_id').eq('customer_id',user.id).order('created_at',{ascending:false});if(!error)orders=data||[];}
  async function showOrders(){await loadOrders();const list=orders.map(o=>`<div class="panel"><div class="row"><div><b>#${esc(o.id.slice(0,8))}</b><p style="margin:5px 0">${esc(restaurants.find(r=>r.id===o.restaurant_id)?.name || 'Restaurant')}</p></div><span class="status">${esc(statusLabel(o.status))}</span></div><p class="muted">${esc(o.delivery_address)} • ${money(o.total)}</p><button class="btn primary" onclick="bfdLiveTrack('${o.id}')">Live Track →</button></div>`).join('');root.innerHTML=frame(`<div class="container"><h2>मेरे Orders</h2>${list||'<div class="empty">🧾<h3>अभी कोई order नहीं</h3><button class="btn primary" onclick="bfdLiveHome()">खाना देखें</button></div>'}</div>`);}

  async function showTracking(id){selectedOrder=orders.find(o=>o.id===id)||selectedOrder;if(!selectedOrder)return; if(cleanupOrder)cleanupOrder();const data=await api.getTracking(id).catch(()=>null);const o=data?.order||selectedOrder;const r=data?.restaurant;const loc=data?.rider_location;root.innerHTML=frame(`<div class="container"><button class="back" onclick="bfdLiveOrders()">← Orders</button><h2>Live Order Tracking</h2><div class="panel"><div class="row"><b>#${esc(o.id.slice(0,8))}</b><span class="status">${esc(statusLabel(o.status))}</span></div><p>${esc(r?.name||'Restaurant')}</p><p class="muted">${esc(o.delivery_address||'')}</p></div><div class="map"><div class="road"></div><div class="route"></div><div class="marker m1">🏨</div><div class="marker m2">📍</div>${loc?'<div class="marker m3">🛵</div>':'<div class="panel" style="position:absolute;inset:auto 12px 12px">Rider location उपलब्ध होते ही यहाँ दिखेगी।</div>'}</div><div class="panel"><h3>Delivery journey</h3><p>✅ Order placed</p><p>${['restaurant_notified','accepted','preparing','ready_for_pickup','rider_assigned','picked_up','on_the_way','delivered'].includes(o.status)?'✅':'○'} Restaurant processing</p><p>${['rider_assigned','picked_up','on_the_way','delivered'].includes(o.status)?'✅':'○'} Rider assigned</p><p>${['picked_up','on_the_way','delivered'].includes(o.status)?'✅':'○'} Pickup completed</p><p>${o.status==='delivered'?'✅':'○'} Delivered</p></div></div>`);cleanupOrder=api.subscribeToOrder(id,async()=>{await showTracking(id);});}

  function search(){root.innerHTML=frame(`<div class="container"><h2>Search</h2><input id="bfd-q" class="search" style="width:100%;border:1px solid #ddd" placeholder="Hotel या food खोजें…" oninput="bfdLiveFilter(this.value)"><div id="bfd-results" class="grid" style="margin-top:16px"></div></div>`);bfdLiveFilter('');}
  function filter(q){const x=q.toLowerCase();const arr=restaurants.filter(r=>(r.name+' '+(r.cuisine||'')+' '+(r.address||'')).toLowerCase().includes(x));document.getElementById('bfd-results').innerHTML=arr.map(r=>`<article class="card"><div class="food-img">🍛</div><div class="card-body"><h3>${esc(r.name)}</h3><p class="muted">${esc(r.cuisine||'Food')}</p><button class="btn primary" onclick="bfdLiveRestaurant('${r.id}')">Menu</button></div></article>`).join('')||'<div class="empty">कोई restaurant नहीं मिला</div>';}

  async function profile(){const user=await api.user();root.innerHTML=frame(`<div class="container"><h2>Profile</h2><div class="panel"><h3>👤 Customer</h3><p>${user?`Signed in as ${esc(user.phone||user.email||'customer')}`:'Login required for ordering'}</p>${user?'<button class="btn secondary" onclick="bfdLiveSignOut()">Sign out</button>':'<button class="btn primary" onclick="bfdLiveAuth()">Login / Sign up</button>'}</div><div class="panel"><h3>Career / Partner with us</h3><p>Restaurant/Hotel Partner या Delivery Rider के लिए आवेदन app के existing partner flow से किया जा सकता है।</p></div></div>`);}

  async function auth(){const phone=prompt('Mobile number दर्ज करें (OTP login)');if(!phone)return;const {error}=await api.client.auth.signInWithOtp({phone});if(error)toast(error.message);else toast('OTP भेज दिया गया — Supabase SMS provider configured होना चाहिए।');}
  async function signOut(){await api.client.auth.signOut();toast('Logout हो गया');profile();}

  function boot(){loadHome();}
  window.bfdLiveHome=loadHome;window.bfdLiveRestaurant=openRestaurant;window.bfdLiveAdd=add;window.bfdLiveCheckout=checkout;window.bfdLivePlaceOrder=placeOrder;window.bfdLiveOrders=showOrders;window.bfdLiveTrack=showTracking;window.bfdLiveSearch=search;window.bfdLiveFilter=filter;window.bfdLiveProfile=profile;window.bfdLiveAuth=auth;window.bfdLiveSignOut=signOut;
  boot();
})();

/* Barmer Food Delivery — production partner application runtime.
 * Activates only when Supabase is configured. Keeps the legacy demo UI intact otherwise.
 */
(function () {
  'use strict';
  const api = window.BFD_SUPABASE;
  if (!api?.ready) return;

  const root = () => document.getElementById('app');
  const esc = (v) => String(v ?? '').replace(/[&<>'"]/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'}[c]));
  const toast = (text) => { const old=document.querySelector('.bfd-partner-toast'); if(old)old.remove(); const el=document.createElement('div'); el.className='toast bfd-partner-toast'; el.textContent=text; document.body.appendChild(el); setTimeout(()=>el.remove(),2800); };
  const frame = (body) => `<div class="app"><header class="top"><div class="brand"><div><div class="logo">🍽️ Barmer Food Delivery</div><div class="location">📍 Barmer, Rajasthan • Partner Centre</div></div><button class="btn secondary" onclick="bfdLiveHome?.()">⌂</button></div></header><main class="page">${body}</main></div>`;
  const input = (id,label,placeholder,type='text') => `<label class="field"><span>${label}</span><input id="${id}" class="search" style="width:100%;border:1px solid #ddd" type="${type}" placeholder="${placeholder}" required></label>`;

  async function requireUser(){
    const user=await api.user();
    if(!user){ toast('पहले Customer login/OTP से sign in करें।'); if(window.bfdLiveAuth) window.bfdLiveAuth(); return null; }
    return user;
  }

  async function applyRestaurant(){
    const user=await requireUser(); if(!user)return;
    const r=await api.client.from('restaurant_applications').insert({
      applicant_id:user.id,
      restaurant_name:document.getElementById('pa-rname').value.trim(),
      owner_name:document.getElementById('pa-owner').value.trim(),
      phone:document.getElementById('pa-phone').value.trim(),
      address:document.getElementById('pa-address').value.trim(),
      barmer_area:document.getElementById('pa-area').value.trim(),
      registration_no:document.getElementById('pa-reg').value.trim()||null
    }).select('id,status').single();
    if(r.error){toast(r.error.message||'आवेदन जमा नहीं हुआ');return;}
    toast('Restaurant/Hotel application जमा हो गया। Admin approval के बाद listing सक्रिय होगी।');
    await showMyApplications();
  }

  async function applyRider(){
    const user=await requireUser(); if(!user)return;
    const r=await api.client.from('rider_applications').insert({
      applicant_id:user.id,
      full_name:document.getElementById('pa-name').value.trim(),
      phone:document.getElementById('pa-phone').value.trim(),
      address:document.getElementById('pa-address').value.trim(),
      vehicle_no:document.getElementById('pa-vehicle').value.trim()||null,
      licence_no:document.getElementById('pa-licence').value.trim()||null,
      emergency_contact:document.getElementById('pa-emergency').value.trim()||null
    }).select('id,status').single();
    if(r.error){toast(r.error.message||'आवेदन जमा नहीं हुआ');return;}
    toast('Delivery Rider application जमा हो गया। Admin approval के बाद rider account सक्रिय होगा।');
    await showMyApplications();
  }

  async function showMyApplications(){
    const user=await requireUser(); if(!user)return;
    const [ra,da]=await Promise.all([
      api.client.from('restaurant_applications').select('id,restaurant_name,status,admin_note,created_at').eq('applicant_id',user.id).order('created_at',{ascending:false}),
      api.client.from('rider_applications').select('id,full_name,status,admin_note,created_at').eq('applicant_id',user.id).order('created_at',{ascending:false})
    ]);
    const rows=[...(ra.data||[]).map(x=>`<div class="panel"><b>🏨 ${esc(x.restaurant_name)}</b><p>Status: <span class="status">${esc(x.status)}</span></p>${x.admin_note?`<p class="muted">Admin: ${esc(x.admin_note)}</p>`:''}</div>`),...(da.data||[]).map(x=>`<div class="panel"><b>🛵 Rider: ${esc(x.full_name)}</b><p>Status: <span class="status">${esc(x.status)}</span></p>${x.admin_note?`<p class="muted">Admin: ${esc(x.admin_note)}</p>`:''}</div>`)];
    root().innerHTML=frame(`<div class="container"><button class="back" onclick="bfdLiveProfile?.()">← Profile</button><h2>Partner / Rider Applications</h2>${rows.join('')||'<div class="empty">अभी कोई application नहीं है।</div>'}<div class="panel"><p class="muted">KYC documents का secure upload अगली backend phase में जोड़ा जाएगा। अभी केवल सत्यापित application data submit होता है।</p></div></div>`);
  }

  function restaurantForm(){root().innerHTML=frame(`<div class="container"><button class="back" onclick="bfdPartnerHome()">← Partner Centre</button><h2>🏨 Restaurant / Hotel Partner</h2><div class="panel">${input('pa-rname','Restaurant / Hotel name','जैसे Marwar Rasoi')}${input('pa-owner','Owner name','मालिक का नाम')}${input('pa-phone','Mobile number','10 digit mobile','tel')}${input('pa-address','Full address','Barmer में पूरा पता')}${input('pa-area','Barmer area','इलाका / वार्ड / कॉलोनी')}${input('pa-reg','Registration / FSSAI / GST no. (optional)','यदि उपलब्ध हो')}<button class="btn primary" style="width:100%" onclick="bfdApplyRestaurant()">Application Submit करें</button></div></div>`);}
  function riderForm(){root().innerHTML=frame(`<div class="container"><button class="back" onclick="bfdPartnerHome()">← Partner Centre</button><h2>🛵 Delivery Rider</h2><div class="panel">${input('pa-name','Full name','पूरा नाम')}${input('pa-phone','Mobile number','10 digit mobile','tel')}${input('pa-address','Address','पूरा पता')}${input('pa-vehicle','Vehicle number','RJ-XX-XX-XXXX')}${input('pa-licence','Driving licence number','Licence number')}${input('pa-emergency','Emergency contact','नाम + मोबाइल')}<button class="btn primary" style="width:100%" onclick="bfdApplyRider()">Application Submit करें</button></div></div>`);}
  function partnerHome(){root().innerHTML=frame(`<div class="container"><button class="back" onclick="bfdLiveProfile?.()">← Profile</button><h2>Career / Partner with us</h2><p class="muted">Barmer Food Delivery network में जुड़ें।</p><div class="panel"><h3>🏨 Restaurant / Hotel</h3><p>अपना restaurant register करें। Approval के बाद ही customer app में दिखाई देगा।</p><button class="btn primary" onclick="bfdRestaurantForm()">Partner बनें</button></div><div class="panel"><h3>🛵 Delivery Rider</h3><p>Rider के रूप में आवेदन करें। Approval के बाद jobs और live delivery tools मिलेंगे।</p><button class="btn primary" onclick="bfdRiderForm()">Rider बनें</button></div><button class="btn secondary" style="width:100%" onclick="bfdMyApplications()">मेरे Applications देखें</button></div>`);}

  window.bfdPartnerHome=partnerHome;
  window.bfdRestaurantForm=restaurantForm;
  window.bfdRiderForm=riderForm;
  window.bfdApplyRestaurant=applyRestaurant;
  window.bfdApplyRider=applyRider;
  window.bfdMyApplications=showMyApplications;
})();

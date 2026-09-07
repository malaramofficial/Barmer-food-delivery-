(function(){
  'use strict';
  const C=window.BFD_CONFIG||{};
  const mapUrl=(lat,lng,z=15)=>`https://www.openstreetmap.org/?mlat=${encodeURIComponent(lat)}&mlon=${encodeURIComponent(lng)}#map=${z}/${encodeURIComponent(lat)}/${encodeURIComponent(lng)}`;
  const navUrl=(lat,lng)=>`https://www.google.com/maps/dir/?api=1&destination=${encodeURIComponent(lat+','+lng)}`;
  function esc(v){return String(v??'').replace(/[&<>"']/g,m=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m]));}
  function hav(a,b,c,d){const R=6371,rad=Math.PI/180,x=(c-a)*rad,y=(d-b)*rad,q=Math.sin(x/2)**2+Math.cos(a*rad)*Math.cos(c*rad)*Math.sin(y/2)**2;return 2*R*Math.asin(Math.sqrt(q));}
  window.BFD_TRACKING_MAP={
    render(container,data){
      if(!container||!data)return;
      const r=data.restaurant, o=data.order, l=data.rider_location;
      const hasR=r&&Number.isFinite(Number(r.latitude))&&Number.isFinite(Number(r.longitude));
      const hasD=Number.isFinite(Number(o?.delivery_latitude))&&Number.isFinite(Number(o?.delivery_longitude));
      const hasL=l&&Number.isFinite(Number(l.latitude))&&Number.isFinite(Number(l.longitude));
      let html='<div class="bfd-live-map-card"><div class="bfd-live-map-title">📍 Live Delivery</div>';
      if(hasR)html+=`<div class="bfd-map-point"><b>🏪 Restaurant</b><span>${esc(r.name)}</span><a href="${mapUrl(r.latitude,r.longitude)}" target="_blank" rel="noopener">Map</a></div>`;
      if(hasL)html+=`<div class="bfd-map-point"><b>🛵 Rider</b><span>Updated ${esc(new Date(l.updated_at).toLocaleTimeString())}${l.is_online?' • Online':' • Offline'}</span><a href="${navUrl(l.latitude,l.longitude)}" target="_blank" rel="noopener">Navigate</a></div>`;
      if(hasD)html+=`<div class="bfd-map-point"><b>📦 Delivery</b><span>${esc(o.delivery_address)}</span><a href="${mapUrl(o.delivery_latitude,o.delivery_longitude)}" target="_blank" rel="noopener">Map</a></div>`;
      if(hasL&&hasD)html+=`<div class="bfd-map-eta">Rider distance to customer: <b>${hav(Number(l.latitude),Number(l.longitude),Number(o.delivery_latitude),Number(o.delivery_longitude)).toFixed(1)} km</b></div>`;
      if(!hasR&&!hasL&&!hasD)html+='<div class="bfd-map-empty">Live coordinates अभी उपलब्ध नहीं हैं। Order location/GPS मिलने पर tracking दिखेगी।</div>';
      html+='</div>'; container.innerHTML=html;
    },
    openRoute(lat,lng){if(Number.isFinite(Number(lat))&&Number.isFinite(Number(lng)))window.open(navUrl(Number(lat),Number(lng)),'_blank','noopener');}
  };
})();

(function(){
  'use strict';
  const mapUrl=(lat,lng,z=15)=>`https://www.openstreetmap.org/?mlat=${encodeURIComponent(lat)}&mlon=${encodeURIComponent(lng)}#map=${z}/${encodeURIComponent(lat)}/${encodeURIComponent(lng)}`;
  const navUrl=(lat,lng)=>`https://www.google.com/maps/dir/?api=1&destination=${encodeURIComponent(lat+','+lng)}`;
  function esc(v){return String(v??'').replace(/[&<>"']/g,m=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m]));}
  function valid(a,b){return Number.isFinite(Number(a))&&Number.isFinite(Number(b));}
  function hav(a,b,c,d){const R=6371,rad=Math.PI/180,x=(c-a)*rad,y=(d-b)*rad,q=Math.sin(x/2)**2+Math.cos(a*rad)*Math.cos(c*rad)*Math.sin(y/2)**2;return 2*R*Math.asin(Math.sqrt(q));}
  let map=null,markers={},route=null,lastKey='';
  function pointIcon(emoji){return L.divIcon({className:'bfd-map-marker',html:`<span>${emoji}</span>`,iconSize:[38,38],iconAnchor:[19,19]});}
  function render(container,data){
    if(!container||!data)return;
    const r=data.restaurant,o=data.order,l=data.rider_location;
    const points=[];
    if(valid(r?.latitude,r?.longitude))points.push({key:'restaurant',lat:Number(r.latitude),lng:Number(r.longitude),emoji:'🏪',title:r.name||'Restaurant'});
    if(valid(l?.latitude,l?.longitude))points.push({key:'rider',lat:Number(l.latitude),lng:Number(l.longitude),emoji:'🛵',title:'Rider'});
    if(valid(o?.delivery_latitude,o?.delivery_longitude))points.push({key:'customer',lat:Number(o.delivery_latitude),lng:Number(o.delivery_longitude),emoji:'📍',title:'Delivery location'});
    const key=points.map(p=>`${p.key}:${p.lat.toFixed(5)},${p.lng.toFixed(5)}`).join('|');
    let mapEl=container.querySelector('.bfd-leaflet-map');
    if(!mapEl){container.innerHTML=`<div class="bfd-live-map-card"><div class="bfd-live-map-title">📍 Live Delivery Tracking</div><div class="bfd-leaflet-map"></div><div class="bfd-map-details"></div></div>`;mapEl=container.querySelector('.bfd-leaflet-map');}
    if(!window.L){container.querySelector('.bfd-map-details').innerHTML='<div class="bfd-map-empty">Map library load नहीं हुई।</div>';return;}
    if(!map){map=L.map(mapEl,{zoomControl:true}).setView(points[0]?[points[0].lat,points[0].lng]:[25.75,71.4],13);L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{maxZoom:19,attribution:'© OpenStreetMap contributors'}).addTo(map);}
    Object.values(markers).forEach(m=>m.remove());markers={};
    points.forEach(p=>{markers[p.key]=L.marker([p.lat,p.lng],{icon:pointIcon(p.emoji)}).addTo(map).bindPopup(`<b>${esc(p.title)}</b>`);});
    if(route)route.remove();
    if(points.length>1){const latlngs=points.map(p=>[p.lat,p.lng]);route=L.polyline(latlngs,{weight:5,opacity:.75}).addTo(map);map.fitBounds(L.latLngBounds(latlngs),{padding:[30,30],maxZoom:15});}
    else if(points.length===1)map.setView([points[0].lat,points[0].lng],15);
    const details=container.querySelector('.bfd-map-details');
    let html=points.map(p=>`<div class="bfd-map-point"><b>${p.emoji} ${esc(p.title)}</b><a href="${p.key==='rider'?navUrl(p.lat,p.lng):mapUrl(p.lat,p.lng)}" target="_blank" rel="noopener">${p.key==='rider'?'Navigate':'Map'}</a></div>`).join('');
    if(valid(l?.latitude,l?.longitude)&&valid(o?.delivery_latitude,o?.delivery_longitude)){const km=hav(Number(l.latitude),Number(l.longitude),Number(o.delivery_latitude),Number(o.delivery_longitude));const eta=Math.max(2,Math.round(km/25*60));html+=`<div class="bfd-map-eta">Rider से delivery तक <b>${km.toFixed(1)} km</b> • अनुमानित <b>${eta} min</b></div>`;}
    if(l?.updated_at)html+=`<div class="muted">Rider GPS: ${l.is_online?'Online':'Offline'} • ${esc(new Date(l.updated_at).toLocaleTimeString())}</div>`;
    if(!points.length)html='<div class="bfd-map-empty">Live coordinates अभी उपलब्ध नहीं हैं। GPS location मिलने पर tracking दिखाई जाएगी।</div>';
    details.innerHTML=html;
    if(key!==lastKey){lastKey=key;setTimeout(()=>map?.invalidateSize(),50);}
  }
  window.BFD_TRACKING_MAP={render,openRoute:(lat,lng)=>{if(valid(lat,lng))window.open(navUrl(Number(lat),Number(lng)),'_blank','noopener');}};
})();

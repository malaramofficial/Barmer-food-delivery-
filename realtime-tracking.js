(function(){
  'use strict';
  const api=window.BFD_SUPABASE;
  if(!api?.ready||!api.client)return;
  let channel=null, timer=null;
  function stop(){if(channel){api.client.removeChannel(channel);channel=null;}if(timer){clearInterval(timer);timer=null;}}
  async function start(orderId,render){
    stop(); if(!orderId||typeof render!=='function')return stop;
    const refresh=async()=>{try{const data=await api.getTracking(orderId);render(data);}catch(_){}};
    await refresh();
    channel=api.client.channel('bfd-live-tracking-'+orderId)
      .on('postgres_changes',{event:'UPDATE',schema:'public',table:'orders',filter:'id=eq.'+orderId},refresh)
      .on('postgres_changes',{event:'INSERT',schema:'public',table:'rider_locations'},async payload=>{if(payload?.new?.rider_id){const data=await api.getTracking(orderId).catch(()=>null);if(data)render(data);}})
      .subscribe();
    timer=setInterval(refresh,10000);
    return stop;
  }
  window.BFD_REALTIME_TRACKING={start,stop};
})();

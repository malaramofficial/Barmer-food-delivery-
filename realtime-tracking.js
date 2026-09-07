(function(){
  'use strict';
  const api=window.BFD_SUPABASE;
  if(!api?.ready||!api.client)return;
  let channel=null,timer=null,activeOrderId=null;
  function stop(){if(channel){api.client.removeChannel(channel);channel=null;}if(timer){clearInterval(timer);timer=null;}activeOrderId=null;}
  async function start(orderId,render){
    stop(); if(!orderId||typeof render!=='function')return stop;
    activeOrderId=orderId;
    const refresh=async()=>{if(activeOrderId!==orderId)return;try{const data=await api.getTracking(orderId);render(data);}catch(_){}};
    await refresh();
    channel=api.client.channel('bfd-live-tracking-'+orderId)
      .on('postgres_changes',{event:'UPDATE',schema:'public',table:'orders',filter:'id=eq.'+orderId},refresh)
      .on('postgres_changes',{event:'INSERT',schema:'public',table:'rider_locations'},payload=>{if(activeOrderId===orderId&&payload?.new?.rider_id)refresh();})
      .subscribe();
    timer=setInterval(refresh,10000);
    return stop;
  }
  window.BFD_REALTIME_TRACKING={start,stop};
  window.addEventListener('beforeunload',stop);
})();

import { createClient } from 'https://esm.sh/@supabase/supabase-js@2';

const cors = {'Access-Control-Allow-Origin':'*','Access-Control-Allow-Headers':'authorization, x-client-info, apikey, content-type'};
const json=(b:unknown,s=200)=>new Response(JSON.stringify(b),{status:s,headers:{...cors,'Content-Type':'application/json'}});
const transitions: Record<string,string[]> = {
  restaurant: ['accepted','preparing','ready_for_pickup'],
  rider: ['picked_up','on_the_way','delivered'],
};

Deno.serve(async req=>{
  if(req.method==='OPTIONS') return new Response('ok',{headers:cors});
  if(req.method!=='POST') return json({error:'Method not allowed'},405);
  const service=createClient(Deno.env.get('SUPABASE_URL')!,Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')!);
  const auth=createClient(Deno.env.get('SUPABASE_URL')!,Deno.env.get('SUPABASE_ANON_KEY')!,{global:{headers:{Authorization:req.headers.get('Authorization')??''}}});
  const {data:{user}}=await auth.auth.getUser(); if(!user)return json({error:'Authentication required'},401);
  const body=await req.json().catch(()=>null);
  if(!body?.order_id||!body?.status)return json({error:'order_id and status are required'},400);
  const {data:profile}=await service.from('profiles').select('role').eq('id',user.id).single();
  const role=profile?.role as 'restaurant'|'rider'|undefined;
  if(!role||!transitions[role]?.includes(body.status))return json({error:'Action not allowed'},403);
  const {data:order}=await service.from('orders').select('id,status,restaurant_id,rider_id').eq('id',body.order_id).single();
  if(!order)return json({error:'Order not found'},404);

  if(role==='restaurant'){
    const {data:owned}=await service.from('restaurants').select('id').eq('id',order.restaurant_id).eq('owner_id',user.id).eq('is_approved',true).single();
    if(!owned)return json({error:'Restaurant access denied'},403);
  } else if(order.rider_id!==user.id){
    return json({error:'Order is not assigned to this rider'},403);
  }

  const allowed: Record<string,string[]> = {
    accepted:['restaurant_notified','placed'],
    preparing:['accepted'],
    ready_for_pickup:['preparing'],
    picked_up:['ready_for_pickup','rider_assigned'],
    on_the_way:['picked_up'],
    delivered:['on_the_way'],
  };
  if(!allowed[body.status]?.includes(order.status))return json({error:`Invalid transition from ${order.status}`},409);

  const patch:any={status:body.status};
  if(body.status==='accepted')patch.accepted_at=new Date().toISOString();
  if(body.status==='picked_up')patch.picked_up_at=new Date().toISOString();
  if(body.status==='delivered')patch.delivered_at=new Date().toISOString();
  const {data:updated,error}=await service.from('orders').update(patch).eq('id',order.id).eq('status',order.status).select('id,status').single();
  if(error||!updated)return json({error:'Order changed; please refresh'},409);

  const recipients:any[]=[];
  const {data:full}=await service.from('orders').select('customer_id,rider_id,restaurant_id,restaurants(owner_id)').eq('id',order.id).single();
  if(full?.customer_id)recipients.push(full.customer_id);
  if(full?.rider_id)recipients.push(full.rider_id);
  const owner=(full as any)?.restaurants?.owner_id; if(owner)recipients.push(owner);
  const unique=[...new Set(recipients)].filter((id)=>id!==user.id);
  if(unique.length)await service.from('notifications').insert(unique.map((user_id)=>({user_id,order_id:order.id,type:'order',title:'ऑर्डर अपडेट',body:`ऑर्डर की स्थिति: ${body.status}`,data:{status:body.status}})));
  return json({ok:true,order_id:updated.id,status:updated.status});
});

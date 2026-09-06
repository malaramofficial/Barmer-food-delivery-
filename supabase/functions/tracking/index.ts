import { createClient } from 'https://esm.sh/@supabase/supabase-js@2';

const cors={'Access-Control-Allow-Origin':'*','Access-Control-Allow-Headers':'authorization, x-client-info, apikey, content-type'};
const json=(b:unknown,s=200)=>new Response(JSON.stringify(b),{status:s,headers:{...cors,'Content-Type':'application/json'}});

Deno.serve(async req=>{
 if(req.method==='OPTIONS')return new Response('ok',{headers:cors});
 if(req.method!=='POST')return json({error:'Method not allowed'},405);
 const service=createClient(Deno.env.get('SUPABASE_URL')!,Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')!);
 const auth=createClient(Deno.env.get('SUPABASE_URL')!,Deno.env.get('SUPABASE_ANON_KEY')!,{global:{headers:{Authorization:req.headers.get('Authorization')??''}}});
 const {data:{user}}=await auth.auth.getUser(); if(!user)return json({error:'Authentication required'},401);
 const body=await req.json().catch(()=>null); if(!body?.order_id)return json({error:'order_id is required'},400);
 const {data:order,error}=await service.from('orders').select('id,customer_id,restaurant_id,rider_id,status,delivery_address,delivery_latitude,delivery_longitude,created_at,accepted_at,picked_up_at,delivered_at').eq('id',body.order_id).maybeSingle();
 if(error)return json({error:error.message},500); if(!order)return json({error:'Order not found'},404);
 let allowed=order.customer_id===user.id||order.rider_id===user.id;
 const {data:restaurant}=await service.from('restaurants').select('id,name,address,latitude,longitude,owner_id').eq('id',order.restaurant_id).maybeSingle();
 if(restaurant?.owner_id===user.id)allowed=true;
 if(!allowed)return json({error:'Not allowed'},403);
 let rider_location=null;
 if(order.rider_id){ const {data:loc}=await service.from('rider_locations').select('latitude,longitude,accuracy_m,heading,updated_at,is_online').eq('rider_id',order.rider_id).maybeSingle(); rider_location=loc??null; }
 return json({order,restaurant:restaurant?{id:restaurant.id,name:restaurant.name,address:restaurant.address,latitude:restaurant.latitude,longitude:restaurant.longitude}:null,rider_location});
});

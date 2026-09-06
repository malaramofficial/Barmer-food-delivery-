import { createClient } from 'https://esm.sh/@supabase/supabase-js@2';
const cors={'Access-Control-Allow-Origin':'*','Access-Control-Allow-Headers':'authorization, x-client-info, apikey, content-type'};
const json=(b:unknown,s=200)=>new Response(JSON.stringify(b),{status:s,headers:{...cors,'Content-Type':'application/json'}});
Deno.serve(async req=>{
 if(req.method==='OPTIONS')return new Response('ok',{headers:cors});
 if(req.method!=='POST')return json({error:'Method not allowed'},405);
 const service=createClient(Deno.env.get('SUPABASE_URL')!,Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')!);
 const auth=createClient(Deno.env.get('SUPABASE_URL')!,Deno.env.get('SUPABASE_ANON_KEY')!,{global:{headers:{Authorization:req.headers.get('Authorization')??''}}});
 const {data:{user}}=await auth.auth.getUser(); if(!user)return json({error:'Authentication required'},401);
 const body=await req.json().catch(()=>null); if(!body?.order_id||!body?.title||!body?.message)return json({error:'order_id, title and message are required'},400);
 const {data:order}=await service.from('orders').select('customer_id,restaurant_id,rider_id').eq('id',body.order_id).maybeSingle(); if(!order)return json({error:'Order not found'},404);
 let restaurantOwner:string|null=null;
 if(order.restaurant_id){const {data:r}=await service.from('restaurants').select('owner_id').eq('id',order.restaurant_id).maybeSingle(); restaurantOwner=r?.owner_id??null;}
 const allowed=[order.customer_id,order.rider_id,restaurantOwner].filter(Boolean) as string[];
 if(!allowed.includes(user.id))return json({error:'Not allowed'},403);
 const unique=[...new Set(allowed)];
 const {error}=await service.from('notifications').insert(unique.map(user_id=>({user_id,order_id:body.order_id,title:String(body.title).slice(0,120),body:String(body.message).slice(0,500)})));
 if(error)return json({error:error.message},500);
 return json({ok:true,recipients:unique.length});
});

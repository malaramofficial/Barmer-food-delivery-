import { createClient } from 'https://esm.sh/@supabase/supabase-js@2';
const cors={'Access-Control-Allow-Origin':'*','Access-Control-Allow-Headers':'authorization, x-client-info, apikey, content-type'};
const json=(b:unknown,s=200)=>new Response(JSON.stringify(b),{status:s,headers:{...cors,'Content-Type':'application/json'}});
Deno.serve(async req=>{
 if(req.method==='OPTIONS')return new Response('ok',{headers:cors}); if(req.method!=='POST')return json({error:'Method not allowed'},405);
 const service=createClient(Deno.env.get('SUPABASE_URL')!,Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')!); const auth=createClient(Deno.env.get('SUPABASE_URL')!,Deno.env.get('SUPABASE_ANON_KEY')!,{global:{headers:{Authorization:req.headers.get('Authorization')??''}}}); const {data:{user}}=await auth.auth.getUser(); if(!user)return json({error:'Authentication required'},401);
 const {data:p}=await service.from('profiles').select('role').eq('id',user.id).maybeSingle(); if(p?.role!=='admin')return json({error:'Admin only'},403);
 const [users,restaurants,riders,orders,pendingR,pendingD]=await Promise.all([
  service.from('profiles').select('id,full_name,phone,role,created_at').order('created_at',{ascending:false}).limit(500),
  service.from('restaurants').select('id,name,area,phone,is_open,is_approved,rating,delivery_fee,owner_id,created_at').order('created_at',{ascending:false}).limit(500),
  service.from('rider_applications').select('id,applicant_id,full_name,phone,vehicle_no,status,created_at').order('created_at',{ascending:false}).limit(500),
  service.from('orders').select('id,status,total,payment_method,created_at,customer_id,restaurant_id,rider_id').order('created_at',{ascending:false}).limit(500),
  service.from('restaurant_applications').select('id,restaurant_name,owner_name,phone,status,created_at').eq('status','pending').order('created_at',{ascending:false}).limit(200),
  service.from('rider_applications').select('id,full_name,phone,vehicle_no,status,created_at').eq('status','pending').order('created_at',{ascending:false}).limit(200)
 ]);
 return json({ok:true,generated_at:new Date().toISOString(),users:users.data??[],restaurants:restaurants.data??[],riders:riders.data??[],orders:orders.data??[],pending_restaurant_applications:pendingR.data??[],pending_rider_applications:pendingD.data??[],errors:[users,restaurants,riders,orders,pendingR,pendingD].filter(x=>x.error).map(x=>x.error?.message)});
});

import { createClient } from 'https://esm.sh/@supabase/supabase-js@2';
const cors={'Access-Control-Allow-Origin':'*','Access-Control-Allow-Headers':'authorization, x-client-info, apikey, content-type'};
const json=(b:unknown,s=200)=>new Response(JSON.stringify(b),{status:s,headers:{...cors,'Content-Type':'application/json'}});
Deno.serve(async req=>{
 if(req.method==='OPTIONS')return new Response('ok',{headers:cors});
 if(req.method!=='POST')return json({error:'Method not allowed'},405);
 const service=createClient(Deno.env.get('SUPABASE_URL')!,Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')!);
 const auth=createClient(Deno.env.get('SUPABASE_URL')!,Deno.env.get('SUPABASE_ANON_KEY')!,{global:{headers:{Authorization:req.headers.get('Authorization')??''}}});
 const {data:{user}}=await auth.auth.getUser();if(!user)return json({error:'Authentication required'},401);
 const body=await req.json().catch(()=>null);if(!body?.order_id)return json({error:'order_id is required'},400);
 const {data:p}=await service.from('profiles').select('role').eq('id',user.id).single();if(p?.role!=='rider')return json({error:'Approved rider only'},403);
 const {data:a}=await service.from('rider_applications').select('id').eq('applicant_id',user.id).eq('status','approved').limit(1);if(!a?.length)return json({error:'Rider approval required'},403);
 const {data:o}=await service.from('orders').select('id,status,rider_id').eq('id',body.order_id).single();if(!o)return json({error:'Order not found'},404);
 if(o.status!=='ready_for_pickup'||o.rider_id)return json({error:'This delivery is no longer available'},409);
 const {error}=await service.from('rider_order_rejections').upsert({rider_id:user.id,order_id:o.id},{onConflict:'rider_id,order_id'});
 if(error)return json({error:error.message},500);
 return json({ok:true,order_id:o.id,rejected:true});
});

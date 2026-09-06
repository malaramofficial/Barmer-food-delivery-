import { createClient } from 'https://esm.sh/@supabase/supabase-js@2';

const cors = {'Access-Control-Allow-Origin':'*','Access-Control-Allow-Headers':'authorization, x-client-info, apikey, content-type'};
const json=(b:unknown,s=200)=>new Response(JSON.stringify(b),{status:s,headers:{...cors,'Content-Type':'application/json'}});

Deno.serve(async req=>{
 if(req.method==='OPTIONS') return new Response('ok',{headers:cors});
 if(req.method!=='POST') return json({error:'Method not allowed'},405);
 const service=createClient(Deno.env.get('SUPABASE_URL')!,Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')!);
 const auth=createClient(Deno.env.get('SUPABASE_URL')!,Deno.env.get('SUPABASE_ANON_KEY')!,{global:{headers:{Authorization:req.headers.get('Authorization')??''}}});
 const {data:{user}}=await auth.auth.getUser(); if(!user)return json({error:'Authentication required'},401);
 const {data:p}=await service.from('profiles').select('role').eq('id',user.id).maybeSingle(); if(p?.role!=='rider')return json({error:'Rider only'},403);
 const {data:a}=await service.from('rider_applications').select('id').eq('applicant_id',user.id).eq('status','approved').limit(1).maybeSingle(); if(!a)return json({error:'Rider approval required'},403);
 const body=await req.json().catch(()=>null); const lat=Number(body?.latitude),lng=Number(body?.longitude);
 const accuracy=body?.accuracy_m==null?null:Number(body.accuracy_m); const heading=body?.heading==null?null:Number(body.heading);
 if(!Number.isFinite(lat)||!Number.isFinite(lng)||lat<-90||lat>90||lng<-180||lng>180)return json({error:'Invalid coordinates'},400);
 if(accuracy!==null&&(!Number.isFinite(accuracy)||accuracy<0))return json({error:'Invalid accuracy'},400);
 if(heading!==null&&(!Number.isFinite(heading)||heading<0||heading>360))return json({error:'Invalid heading'},400);
 const {data:current}=await service.from('rider_locations').select('is_online').eq('rider_id',user.id).maybeSingle();
 const {error}=await service.from('rider_locations').upsert({rider_id:user.id,latitude:lat,longitude:lng,accuracy_m:accuracy,heading:heading,is_online:current?.is_online??false,updated_at:new Date().toISOString()});
 if(error)return json({error:error.message},500); return json({ok:true,is_online:current?.is_online??false});
});

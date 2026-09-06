import { createClient } from 'https://esm.sh/@supabase/supabase-js@2';
const cors={'Access-Control-Allow-Origin':'*','Access-Control-Allow-Headers':'authorization, x-client-info, apikey, content-type'};
const json=(b:unknown,s=200)=>new Response(JSON.stringify(b),{status:s,headers:{...cors,'Content-Type':'application/json'}});
Deno.serve(async req=>{
 if(req.method==='OPTIONS') return new Response('ok',{headers:cors});
 if(req.method!=='POST') return json({error:'Method not allowed'},405);
 const supabase=createClient(Deno.env.get('SUPABASE_URL')!,Deno.env.get('SUPABASE_ANON_KEY')!,{global:{headers:{Authorization:req.headers.get('Authorization')??''}}});
 const {data:{user}}=await supabase.auth.getUser(); if(!user) return json({error:'Authentication required'},401);
 const {data:p}=await supabase.from('profiles').select('role').eq('id',user.id).maybeSingle(); if(p?.role!=='rider') return json({error:'Rider only'},403);
 const body=await req.json().catch(()=>null); const lat=Number(body?.latitude),lng=Number(body?.longitude),accuracy=Number(body?.accuracy_m||0);
 if(!Number.isFinite(lat)||!Number.isFinite(lng)||lat<-90||lat>90||lng<-180||lng>180) return json({error:'Invalid coordinates'},400);
 const {error}=await supabase.from('rider_locations').upsert({rider_id:user.id,latitude:lat,longitude:lng,accuracy_m:Number.isFinite(accuracy)?accuracy:null,heading:Number.isFinite(Number(body?.heading))?Number(body.heading):null,updated_at:new Date().toISOString()});
 if(error)return json({error:error.message},400); return json({ok:true});
});

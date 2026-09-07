package com.malaramofficial.barmerfooddelivery;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Dependency-free Supabase REST/Auth client for the native Android app. */
public final class NativeApi {
    public interface Callback { void ok(JSONObject data); void error(String message); }
    private static final String PREF="bfd_session";
    private final SharedPreferences prefs; private final ExecutorService io=Executors.newCachedThreadPool();
    private final String base,anon;
    public NativeApi(Context c){prefs=c.getApplicationContext().getSharedPreferences(PREF,Context.MODE_PRIVATE);base=BuildConfig.SUPABASE_URL==null?"":BuildConfig.SUPABASE_URL.trim().replaceAll("/$","");anon=BuildConfig.SUPABASE_ANON_KEY==null?"":BuildConfig.SUPABASE_ANON_KEY.trim();}
    public boolean configured(){return !base.isEmpty()&&!anon.isEmpty();} public String accessToken(){return prefs.getString("access_token","");} public String userId(){return prefs.getString("user_id","");} public boolean signedIn(){return !accessToken().isEmpty();} public void signOut(){prefs.edit().clear().apply();}
    public void signIn(String e,String p,Callback cb){auth("/auth/v1/token?grant_type=password",e,p,cb);}
    public void signUp(String e,String p,String n,String ph,Callback cb){JSONObject b=new JSONObject();try{b.put("email",e);b.put("password",p);JSONObject d=new JSONObject();d.put("full_name",n);d.put("phone",ph);b.put("data",d);}catch(Exception x){cb.error(x.getMessage());return;}request("POST","/auth/v1/signup",b,false,new Callback(){public void ok(JSONObject d){saveSession(d);cb.ok(d);}public void error(String m){cb.error(m);}});}
    private void auth(String path,String e,String p,Callback cb){JSONObject b=new JSONObject();try{b.put("email",e);b.put("password",p);}catch(Exception x){cb.error(x.getMessage());return;}request("POST",path,b,false,new Callback(){public void ok(JSONObject d){saveSession(d);cb.ok(d);}public void error(String m){cb.error(m);}});}
    private void saveSession(JSONObject d){try{JSONObject u=d.optJSONObject("user");prefs.edit().putString("access_token",d.optString("access_token","")).putString("refresh_token",d.optString("refresh_token","")).putString("user_id",u==null?"":u.optString("id","")).apply();}catch(Exception ignored){}}
    public void restaurants(Callback cb){request("GET","/rest/v1/restaurants?select=id,name,cuisine,address,area,rating,delivery_fee,is_open,latitude,longitude&is_approved=eq.true&order=name",null,true,cb);}
    public void menu(String id,Callback cb){request("GET","/rest/v1/menu_items?select=id,name,description,price,image_url,is_available&restaurant_id=eq."+id+"&is_available=eq.true&order=name",null,true,cb);}
    public void orders(Callback cb){request("GET","/rest/v1/orders?select=id,restaurant_id,status,delivery_address,delivery_latitude,delivery_longitude,subtotal,delivery_fee,total,payment_method,created_at&order=created_at.desc&limit=30",null,true,cb);}
    public void notifications(Callback cb){request("GET","/rest/v1/notifications?select=id,title,body,order_id,read_at,created_at&order=created_at.desc&limit=30",null,true,cb);}
    public void profile(Callback cb){request("GET","/rest/v1/profiles?select=id,full_name,phone,role,address&limit=1",null,true,cb);}
    public void createOrder(String restaurantId,String address,double lat,double lng,String payment,JSONArray items,Callback cb){JSONObject b=new JSONObject();try{b.put("restaurant_id",restaurantId);b.put("address",address);b.put("latitude",lat);b.put("longitude",lng);b.put("payment_method",payment);b.put("items",items);}catch(Exception e){cb.error(e.getMessage());return;}invoke("create-order",b,cb);}
    public void applyRestaurant(JSONObject b,Callback cb){insert("restaurant_applications",b,cb);} public void applyRider(JSONObject b,Callback cb){insert("rider_applications",b,cb);}
    public void invoke(String fn,JSONObject body,Callback cb){request("POST","/functions/v1/"+fn,body,true,cb);}
    private void insert(String table,JSONObject body,Callback cb){request("POST","/rest/v1/"+table,body,true,cb);}
    private void request(String method,String path,JSONObject body,boolean auth,Callback cb){io.execute(()->{HttpURLConnection c=null;try{if(!configured()){cb.error("Supabase configuration missing");return;}c=(HttpURLConnection)new URL(base+path).openConnection();c.setRequestMethod(method);c.setConnectTimeout(15000);c.setReadTimeout(20000);c.setRequestProperty("apikey",anon);c.setRequestProperty("Accept","application/json");if(auth){String t=accessToken();if(t.isEmpty()){cb.error("Please login first");return;}c.setRequestProperty("Authorization","Bearer "+t);}if(body!=null){c.setRequestProperty("Content-Type","application/json");c.setRequestProperty("Prefer","return=representation");c.setDoOutput(true);try(OutputStream o=c.getOutputStream()){o.write(body.toString().getBytes(StandardCharsets.UTF_8));}}int code=c.getResponseCode();String s=read(code>=200&&code<300?c.getInputStream():c.getErrorStream());JSONObject out;try{out=s.trim().startsWith("[")?new JSONObject().put("data",new JSONArray(s)):new JSONObject(s);}catch(Exception e){out=new JSONObject().put("raw",s);}if(code>=200&&code<300)cb.ok(out);else cb.error(out.optString("message",out.optString("error",s.length()>300?s.substring(0,300):s)));}catch(Exception e){cb.error(e.getMessage()==null?"Network error":e.getMessage());}finally{if(c!=null)c.disconnect();}});}
    private String read(InputStream in)throws IOException{if(in==null)return "";StringBuilder b=new StringBuilder();try(BufferedReader r=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8))){String l;while((l=r.readLine())!=null)b.append(l);}return b.toString();}
}

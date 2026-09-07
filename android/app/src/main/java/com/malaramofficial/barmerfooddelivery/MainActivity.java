package com.malaramofficial.barmerfooddelivery;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.content.Context;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

/** Native Android customer UI. No WebView/browser-hosted screens. */
public class MainActivity extends Activity {
    private static final int LOC = 1001;
    private NativeApi api;
    private LinearLayout root, content;
    private final ArrayList<JSONObject> cart = new ArrayList<>();
    private String restaurantId = "", restaurantName = "";
    private double lat, lng;

    private static final int PRIMARY = Color.rgb(232, 83, 37);
    private static final int PRIMARY_DARK = Color.rgb(190, 61, 25);
    private static final int INK = Color.rgb(30, 30, 32);
    private static final int MUTED = Color.rgb(104, 104, 110);
    private static final int SURFACE = Color.WHITE;
    private static final int BG = Color.rgb(248, 247, 245);
    private static final int SOFT = Color.rgb(255, 239, 231);
    private static final int GREEN = Color.rgb(30, 137, 77);

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(SURFACE);
        getWindow().setNavigationBarColor(SURFACE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        api = new NativeApi(this);
        showHome();
    }

    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }
    private TextView tv(String s, float size, int color) {
        TextView v = new TextView(this);
        v.setText(s); v.setTextSize(size); v.setTextColor(color);
        v.setFontFeatureSettings("kern");
        return v;
    }
    private TextView label(String s) { TextView v = tv(s, 13, MUTED); v.setTypeface(Typeface.DEFAULT, Typeface.BOLD); return v; }
    private TextView h1(String s) { TextView v = tv(s, 27, INK); v.setTypeface(Typeface.DEFAULT, Typeface.BOLD); return v; }
    private TextView h2(String s) { TextView v = tv(s, 19, INK); v.setTypeface(Typeface.DEFAULT, Typeface.BOLD); return v; }
    private GradientDrawable shape(int color, float radius) { GradientDrawable g = new GradientDrawable(); g.setColor(color); g.setCornerRadius(dp((int)radius)); return g; }
    private GradientDrawable stroke(int color, int line, float radius) { GradientDrawable g = shape(SURFACE, radius); g.setStroke(dp(line), color); return g; }

    private void margin(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof LinearLayout.LayoutParams) {
            ((LinearLayout.LayoutParams)v.getLayoutParams()).setMargins(dp(l),dp(t),dp(r),dp(b));
        }
    }
    private void add(View v) { content.addView(v, new LinearLayout.LayoutParams(-1, -2)); }
    private void add(View v, int top, int bottom) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2); p.setMargins(0,dp(top),0,dp(bottom)); content.addView(v,p);
    }
    private Space space(int h) { Space s = new Space(this); s.setLayoutParams(new LinearLayout.LayoutParams(1,dp(h))); return s; }

    private Button primary(String text) {
        Button b = new Button(this);
        b.setText(text); b.setTextSize(15); b.setAllCaps(false); b.setTextColor(Color.WHITE);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD); b.setMinHeight(dp(52)); b.setPadding(dp(16),0,dp(16),0);
        b.setBackground(shape(PRIMARY, 16)); b.setElevation(dp(1));
        return b;
    }
    private Button secondary(String text) {
        Button b = new Button(this);
        b.setText(text); b.setTextSize(15); b.setAllCaps(false); b.setTextColor(INK);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD); b.setMinHeight(dp(50)); b.setPadding(dp(14),0,dp(14),0);
        b.setBackground(stroke(Color.rgb(225,224,221),1,16));
        return b;
    }
    private TextView iconTile(String icon, String title, String sub, int color) {
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setGravity(Gravity.CENTER);
        box.setPadding(dp(10),dp(10),dp(10),dp(10)); box.setBackground(shape(SURFACE,18)); box.setElevation(dp(1));
        TextView i = tv(icon,24,color); i.setGravity(Gravity.CENTER); box.addView(i,new LinearLayout.LayoutParams(-1,dp(34)));
        TextView t = tv(title,13,INK); t.setGravity(Gravity.CENTER); t.setTypeface(Typeface.DEFAULT,Typeface.BOLD); box.addView(t);
        TextView s = tv(sub,11,MUTED); s.setGravity(Gravity.CENTER); box.addView(s);
        return wrapTile(box);
    }
    private TextView wrapTile(LinearLayout box) { TextView v = new TextView(this); v.setVisibility(View.GONE); box.setTag(v); return v; }

    private void base(String title) {
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(BG);
        root.setPadding(dp(18),dp(8),dp(18),0);

        LinearLayout bar = new LinearLayout(this); bar.setGravity(Gravity.CENTER_VERTICAL);
        TextView back = tv("‹",34,INK); back.setGravity(Gravity.CENTER); back.setVisibility("Barmer Food Delivery".equals(title) ? View.GONE : View.VISIBLE);
        back.setOnClickListener(v -> showHome());
        bar.addView(back,new LinearLayout.LayoutParams(dp(42),dp(54)));
        TextView t = h2(title); bar.addView(t,new LinearLayout.LayoutParams(0,dp(54),1));
        TextView profile = tv("◉",24,PRIMARY); profile.setGravity(Gravity.CENTER); profile.setContentDescription("Profile");
        profile.setOnClickListener(v -> showProfile()); bar.addView(profile,new LinearLayout.LayoutParams(dp(44),dp(54)));
        root.addView(bar);

        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.setClipToPadding(false);
        content = new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(0,dp(4),0,dp(28));
        scroll.addView(content); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL); c.setPadding(dp(16),dp(15),dp(16),dp(15));
        c.setBackground(shape(SURFACE,20)); c.setElevation(dp(1));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,-2); p.setMargins(0,dp(5),0,dp(5)); c.setLayoutParams(p); return c;
    }
    private TextView chip(String text, boolean selected) {
        TextView v=tv(text,13,selected?PRIMARY:INK); v.setGravity(Gravity.CENTER); v.setPadding(dp(15),0,dp(15),0); v.setBackground(shape(selected?SOFT:SURFACE,22));
        v.setTypeface(Typeface.DEFAULT,Typeface.BOLD); return v;
    }

    private void showHome() {
        base("Barmer Food Delivery");
        LinearLayout hero = new LinearLayout(this); hero.setOrientation(LinearLayout.VERTICAL); hero.setPadding(dp(20),dp(22),dp(20),dp(22)); hero.setBackground(shape(PRIMARY,24));
        TextView small=tv("BARmer • FOOD • DELIVERY",12,Color.WHITE); small.setTypeface(Typeface.DEFAULT,Typeface.BOLD); hero.addView(small);
        TextView main=tv("भूख लगी है?\nआज क्या खाएँ?",28,Color.WHITE); main.setTypeface(Typeface.DEFAULT,Typeface.BOLD); hero.addView(main,new LinearLayout.LayoutParams(-1,-2));
        TextView sub=tv("बारमेर के पसंदीदा restaurants से\nखाना सीधे आपके दरवाज़े तक।",14,Color.rgb(255,240,234)); hero.addView(sub);
        LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,-2); hp.setMargins(0,dp(5),0,dp(10)); content.addView(hero,hp);

        LinearLayout search = new LinearLayout(this); search.setGravity(Gravity.CENTER_VERTICAL); search.setPadding(dp(16),0,dp(10),0); search.setBackground(shape(SURFACE,18)); search.setElevation(dp(1));
        TextView glass=tv("⌕",25,MUTED); search.addView(glass,new LinearLayout.LayoutParams(dp(30),dp(54)));
        EditText q=new EditText(this); q.setHint("Restaurant या dish खोजें"); q.setTextSize(15); q.setSingleLine(); q.setBackgroundColor(Color.TRANSPARENT); search.addView(q,new LinearLayout.LayoutParams(0,dp(54),1));
        TextView filter=tv("☷",22,PRIMARY); filter.setGravity(Gravity.CENTER); search.addView(filter,new LinearLayout.LayoutParams(dp(40),dp(54)));
        LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(54)); sp.setMargins(0,0,0,dp(12)); content.addView(search,sp);

        LinearLayout loc=new LinearLayout(this); loc.setGravity(Gravity.CENTER_VERTICAL); loc.setPadding(dp(14),dp(10),dp(14),dp(10)); loc.setBackground(shape(SOFT,16));
        TextView li=tv("⌖",23,PRIMARY); loc.addView(li,new LinearLayout.LayoutParams(dp(34),dp(38)));
        LinearLayout lt=new LinearLayout(this); lt.setOrientation(LinearLayout.VERTICAL); TextView a=label("DELIVER TO"); lt.addView(a); TextView b=tv(lat==0?"Set your location":"Current location set",14,INK); b.setTypeface(Typeface.DEFAULT,Typeface.BOLD); lt.addView(b); loc.addView(lt,new LinearLayout.LayoutParams(0,-2,1));
        Button set=secondary("Change"); set.setTextSize(12); set.setMinHeight(dp(38)); set.setOnClickListener(v->requestLocation()); loc.addView(set,new LinearLayout.LayoutParams(dp(86),dp(42)));
        content.addView(loc,new LinearLayout.LayoutParams(-1,-2));

        TextView sec=h2("Explore food"); add(sec,18,8);
        LinearLayout tiles=new LinearLayout(this); tiles.setGravity(Gravity.CENTER); String[][] cats={{"🍛","Meals","Popular"},{"🍕","Fast food","Quick"},{"🥤","Drinks","Cool"},{"🍰","Dessert","Sweet"}};
        for(String[] c:cats){ LinearLayout tile=new LinearLayout(this); tile.setOrientation(LinearLayout.VERTICAL); tile.setGravity(Gravity.CENTER); tile.setPadding(dp(7),dp(9),dp(7),dp(9)); tile.setBackground(shape(SURFACE,18)); tile.setElevation(dp(1)); TextView ic=tv(c[0],23,INK); ic.setGravity(Gravity.CENTER); tile.addView(ic,new LinearLayout.LayoutParams(-1,dp(31))); TextView nm=tv(c[1],11,INK); nm.setGravity(Gravity.CENTER); nm.setTypeface(Typeface.DEFAULT,Typeface.BOLD); tile.addView(nm); LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(0,dp(76),1); tp.setMargins(dp(3),0,dp(3),0); tiles.addView(tile,tp); }
        content.addView(tiles);

        LinearLayout rh=new LinearLayout(this); rh.setGravity(Gravity.CENTER_VERTICAL); TextView rt=h2("Restaurants near you"); rh.addView(rt,new LinearLayout.LayoutParams(0,-2,1)); TextView all=tv("See all  ›",13,PRIMARY); all.setTypeface(Typeface.DEFAULT,Typeface.BOLD); rh.addView(all); content.addView(rh,new LinearLayout.LayoutParams(-1,-2));
        loadRestaurants();

        LinearLayout quick=new LinearLayout(this); quick.setGravity(Gravity.CENTER); quick.setPadding(0,dp(12),0,0);
        TextView orders=tv("📦\nOrders",12,INK); orders.setGravity(Gravity.CENTER); orders.setTypeface(Typeface.DEFAULT,Typeface.BOLD); orders.setOnClickListener(v->showOrders());
        TextView notices=tv("🔔\nAlerts",12,INK); notices.setGravity(Gravity.CENTER); notices.setTypeface(Typeface.DEFAULT,Typeface.BOLD); notices.setOnClickListener(v->showNotifications());
        TextView partner=tv("🤝\nPartner",12,INK); partner.setGravity(Gravity.CENTER); partner.setTypeface(Typeface.DEFAULT,Typeface.BOLD); partner.setOnClickListener(v->showPartner());
        quick.addView(orders,new LinearLayout.LayoutParams(0,dp(62),1)); quick.addView(notices,new LinearLayout.LayoutParams(0,dp(62),1)); quick.addView(partner,new LinearLayout.LayoutParams(0,dp(62),1)); content.addView(quick);
    }

    private void loadRestaurants() {
        if(!api.configured()){ TextView e=tv("Live restaurants will appear here after Supabase is configured.",13,MUTED); e.setPadding(dp(2),dp(10),0,dp(10)); add(e); return; }
        api.restaurants(new NativeApi.Callback(){ public void ok(JSONObject r){ runOnUiThread(()->{ try{ JSONArray a=r.optJSONArray("data"); if(a==null||a.length()==0){ add(tv("अभी कोई approved restaurant उपलब्ध नहीं है.",14,MUTED),10,10); return; }
            for(int i=0;i<a.length();i++){ JSONObject x=a.getJSONObject(i); LinearLayout c=card(); LinearLayout row=new LinearLayout(MainActivity.this); row.setGravity(Gravity.CENTER_VERTICAL);
                TextView pic=tv("🍽",27,PRIMARY); pic.setGravity(Gravity.CENTER); pic.setBackground(shape(SOFT,18)); row.addView(pic,new LinearLayout.LayoutParams(dp(58),dp(58)));
                LinearLayout info=new LinearLayout(MainActivity.this); info.setOrientation(LinearLayout.VERTICAL); info.setPadding(dp(12),0,dp(6),0); TextView n=tv(x.optString("name"),17,INK); n.setTypeface(Typeface.DEFAULT,Typeface.BOLD); info.addView(n);
                TextView meta=tv(x.optString("cuisine","Food")+"  •  "+x.optString("area","Barmer"),12,MUTED); info.addView(meta); TextView status=tv(x.optBoolean("is_open")?"● Open  •  ₹"+x.optDouble("delivery_fee",0)+" delivery":"● Closed",12,x.optBoolean("is_open")?GREEN:MUTED); status.setTypeface(Typeface.DEFAULT,Typeface.BOLD); info.addView(status); row.addView(info,new LinearLayout.LayoutParams(0,-2,1));
                TextView arrow=tv("›",28,PRIMARY); arrow.setGravity(Gravity.CENTER); row.addView(arrow,new LinearLayout.LayoutParams(dp(30),dp(54))); c.addView(row);
                c.setOnClickListener(v->{if(x.optBoolean("is_open"))showMenu(x);else Toast.makeText(MainActivity.this,"Restaurant अभी बंद है",Toast.LENGTH_SHORT).show();}); content.addView(c);
            }
        }catch(Exception e){add(tv("Restaurant data error: "+e.getMessage(),13,MUTED));}}); } public void error(String m){runOnUiThread(()->add(tv("Restaurants: "+m,13,MUTED)));} });
    }

    private void showMenu(JSONObject r) {
        restaurantId=r.optString("id"); restaurantName=r.optString("name"); cart.clear(); base(restaurantName);
        TextView sub=tv("🍴  "+r.optString("cuisine","Food")+"   •   " + r.optString("area","Barmer"),13,MUTED); add(sub,0,10);
        LinearLayout chips=new LinearLayout(this); chips.addView(chip("Popular",true),new LinearLayout.LayoutParams(-2,dp(38))); chips.addView(chip("Recommended",false),new LinearLayout.LayoutParams(-2,dp(38))); add(chips,0,8);
        api.menu(restaurantId,new NativeApi.Callback(){public void ok(JSONObject z){runOnUiThread(()->{try{JSONArray a=z.optJSONArray("data");if(a==null||a.length()==0){add(tv("Menu उपलब्ध नहीं है.",14,MUTED),10,10);return;}for(int i=0;i<a.length();i++){JSONObject x=a.getJSONObject(i);LinearLayout c=card();LinearLayout row=new LinearLayout(MainActivity.this);row.setGravity(Gravity.CENTER_VERTICAL);
            TextView pic=tv("🍛",25,PRIMARY);pic.setGravity(Gravity.CENTER);pic.setBackground(shape(SOFT,16));row.addView(pic,new LinearLayout.LayoutParams(dp(64),dp(64)));
            LinearLayout info=new LinearLayout(MainActivity.this);info.setOrientation(LinearLayout.VERTICAL);info.setPadding(dp(12),0,dp(8),0);TextView n=tv(x.optString("name"),16,INK);n.setTypeface(Typeface.DEFAULT,Typeface.BOLD);info.addView(n);TextView d=tv(x.optString("description","Delicious and freshly prepared"),12,MUTED);d.setMaxLines(2);info.addView(d);TextView p=tv("₹"+x.optDouble("price",0),15,PRIMARY_DARK);p.setTypeface(Typeface.DEFAULT,Typeface.BOLD);info.addView(p);row.addView(info,new LinearLayout.LayoutParams(0,-2,1));Button addBtn=primary("+ Add");addBtn.setTextSize(13);addBtn.setMinHeight(dp(42));addBtn.setPadding(dp(8),0,dp(8),0);addBtn.setOnClickListener(v->{cart.add(x);Toast.makeText(MainActivity.this,"Cart में जोड़ा ✓",Toast.LENGTH_SHORT).show();});row.addView(addBtn,new LinearLayout.LayoutParams(dp(78),dp(44)));c.addView(row);content.addView(c);}Button cartBtn=primary("View cart  •  "+cart.size()+" items");cartBtn.setOnClickListener(v->showCart());add(cartBtn,12,0);}catch(Exception e){add(tv(e.getMessage(),13,MUTED));}});}public void error(String m){runOnUiThread(()->add(tv("Menu: "+m,13,MUTED)));}});
    }

    private void showCart(){base("Your cart");if(cart.isEmpty()){empty("🛒","Your cart is empty","Add something delicious from a nearby restaurant.","Browse restaurants",this::showHome);return;}double total=0;for(JSONObject x:cart){double p=x.optDouble("price",0);total+=p;LinearLayout c=card();LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);TextView n=tv(x.optString("name"),15,INK);n.setTypeface(Typeface.DEFAULT,Typeface.BOLD);row.addView(n,new LinearLayout.LayoutParams(0,-2,1));row.addView(tv("₹"+p,15,PRIMARY_DARK),new LinearLayout.LayoutParams(dp(72),-2));c.addView(row);add(c);}LinearLayout totalBox=card();TextView t=tv("Subtotal",13,MUTED);totalBox.addView(t);TextView amount=tv("₹"+total,24,INK);amount.setTypeface(Typeface.DEFAULT,Typeface.BOLD);totalBox.addView(amount);add(totalBox,10,8);Button next=primary("Proceed to checkout  →");next.setOnClickListener(v->showCheckout());add(next);}

    private void showCheckout(){base("Checkout");TextView rest=tv("ORDERING FROM",12,MUTED);rest.setTypeface(Typeface.DEFAULT,Typeface.BOLD);add(rest);TextView rn=tv(restaurantName,19,INK);rn.setTypeface(Typeface.DEFAULT,Typeface.BOLD);add(rn,0,12);
        EditText address=input("Delivery address",false);add(address,0,8);Button loc=secondary("⌖  Use current location");loc.setOnClickListener(v->requestLocation());add(loc,0,14);
        TextView pay=label("PAYMENT METHOD");add(pay);LinearLayout cash=card();cash.setBackground(shape(SOFT,18));cash.addView(tv("💵  Cash on Delivery",16,INK));cash.addView(tv("Pay when your order arrives",12,MUTED));add(cash,4,14);
        Button place=primary("Place order  •  COD");place.setOnClickListener(v->{if(!api.signedIn()){openLogin();return;}String ad=address.getText().toString().trim();if(ad.length()<5){address.setError("पूरा address लिखें");return;}if(lat==0||lng==0){Toast.makeText(this,"पहले location सेट करें",Toast.LENGTH_LONG).show();return;}try{JSONArray items=new JSONArray();for(JSONObject x:cart){JSONObject q=new JSONObject();q.put("menu_item_id",x.optString("id"));q.put("quantity",1);items.put(q);}place.setEnabled(false);api.createOrder(restaurantId,ad,lat,lng,"cod",items,new NativeApi.Callback(){public void ok(JSONObject d){runOnUiThread(()->{cart.clear();Toast.makeText(MainActivity.this,"Order placed ✓",Toast.LENGTH_LONG).show();showOrders();});}public void error(String m){runOnUiThread(()->{place.setEnabled(true);Toast.makeText(MainActivity.this,m,Toast.LENGTH_LONG).show();});}});}catch(Exception e){Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();}});add(place);}

    private void showOrders(){base("My orders");if(!api.signedIn()){empty("🔐","Login required","Sign in to see your orders and live status.","Login / Sign up",this::openLogin);return;}api.orders(new NativeApi.Callback(){public void ok(JSONObject d){runOnUiThread(()->{try{JSONArray a=d.optJSONArray("data");if(a==null||a.length()==0){empty("📦","No orders yet","Your placed orders will appear here.","Browse restaurants",MainActivity.this::showHome);return;}for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);String id=o.optString("id");LinearLayout c=card();LinearLayout top=new LinearLayout(MainActivity.this);top.setGravity(Gravity.CENTER_VERTICAL);TextView n=tv("Order #"+id.substring(0,Math.min(8,id.length())),16,INK);n.setTypeface(Typeface.DEFAULT,Typeface.BOLD);top.addView(n,new LinearLayout.LayoutParams(0,-2,1));TextView st=tv(o.optString("status","pending"),12,PRIMARY_DARK);st.setPadding(dp(10),dp(6),dp(10),dp(6));st.setBackground(shape(SOFT,20));top.addView(st);c.addView(top);c.addView(tv("₹"+o.optDouble("total",0)+"  •  "+o.optString("payment_method","cod").toUpperCase(),13,MUTED));c.addView(tv(o.optString("delivery_address"),13,MUTED));Button tr=primary("Track order  →");tr.setMinHeight(dp(44));tr.setOnClickListener(v->showTracking(o));c.addView(tr,new LinearLayout.LayoutParams(-1,dp(46)));content.addView(c);} }catch(Exception e){add(tv(e.getMessage(),13,MUTED));}});}public void error(String m){runOnUiThread(()->add(tv("Orders: "+m,13,MUTED)));}});}

    private void showTracking(JSONObject o){base("Track order");LinearLayout status=card();status.setBackground(shape(SOFT,20));TextView s=tv("●  "+o.optString("status","pending"),18,PRIMARY_DARK);s.setTypeface(Typeface.DEFAULT,Typeface.BOLD);status.addView(s);status.addView(tv("Your order is being processed. Live rider/map integration will show here when the delivery is assigned.",13,MUTED));add(status,0,10);double a=o.optDouble("delivery_latitude",0),b=o.optDouble("delivery_longitude",0);if(a!=0&&b!=0){Button nav=primary("🧭  Navigate to delivery location");nav.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse("google.navigation:q="+a+","+b));try{startActivity(i);}catch(Exception e){startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("geo:"+a+","+b+"?q="+a+","+b)));}});add(nav);} }

    private void showNotifications(){base("Notifications");if(!api.signedIn()){empty("🔔","You're all caught up","Login to receive order and delivery updates.","Login",this::openLogin);return;}api.notifications(new NativeApi.Callback(){public void ok(JSONObject d){runOnUiThread(()->{JSONArray a=d.optJSONArray("data");if(a==null||a.length()==0){empty("🔔","No notifications","Order updates will appear here.","Back home",MainActivity.this::showHome);return;}for(int i=0;i<a.length();i++){JSONObject n=a.optJSONObject(i);LinearLayout c=card();c.addView(tv("🔔  "+n.optString("title"),16,INK));c.addView(tv(n.optString("body"),13,MUTED));content.addView(c);}});}public void error(String m){runOnUiThread(()->add(tv(m,13,MUTED)));}});}

    private void showProfile(){base("Profile");if(!api.signedIn()){empty("👤","Welcome","Sign in to manage your account, orders and partner profile.","Login / Sign up",this::openLogin);}else{api.profile(new NativeApi.Callback(){public void ok(JSONObject d){runOnUiThread(()->{JSONArray a=d.optJSONArray("data");JSONObject p=a==null?null:a.optJSONObject(0);LinearLayout c=card();c.addView(tv("👤  "+(p==null?"Customer":p.optString("full_name","Customer")),20,INK));c.addView(tv("📱  "+(p==null?"":p.optString("phone","")),14,MUTED));c.addView(tv("Role  •  "+(p==null?"customer":p.optString("role","customer")),13,MUTED));add(c);Button sign=secondary("Sign out");sign.setOnClickListener(v->{api.signOut();showHome();});add(sign,8,8);});}public void error(String m){runOnUiThread(()->add(tv(m,13,MUTED)));}});}Button p=secondary("🤝  Careers / Partner with us");p.setOnClickListener(v->showPartner());add(p);}



    private EditText input(String hint, boolean password){EditText e=new EditText(this);e.setHint(hint);e.setTextSize(15);e.setSingleLine(!hint.toLowerCase().contains("address"));e.setPadding(dp(16),0,dp(16),0);e.setBackground(stroke(Color.rgb(224,222,218),1,16));if(password)e.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);return e;}
    private NativeApi.Callback result(String ok){return new NativeApi.Callback(){public void ok(JSONObject d){runOnUiThread(()->{Toast.makeText(MainActivity.this,ok,Toast.LENGTH_SHORT).show();showHome();});}public void error(String m){runOnUiThread(()->Toast.makeText(MainActivity.this,m,Toast.LENGTH_LONG).show());}};}

    private void openLogin(){startActivity(new Intent(this, LoginActivity.class));}

    private void showPartner(){base("Partner with us");add(tv("Grow with Barmer Food Delivery",24,INK),8,4);add(tv("Choose how you want to join our delivery network.",14,MUTED),0,14);LinearLayout r=card();r.addView(tv("🏨  Restaurant / Hotel",20,INK));r.addView(tv("List your restaurant, menu and delivery area.",13,MUTED));Button rb=primary("Apply as partner  →");rb.setOnClickListener(v->restaurantForm());r.addView(rb,new LinearLayout.LayoutParams(-1,dp(46)));add(r);LinearLayout d=card();d.addView(tv("🛵  Delivery Rider",20,INK));d.addView(tv("Deliver nearby orders and earn from completed deliveries.",13,MUTED));Button db=secondary("Apply as rider  →");db.setOnClickListener(v->riderForm());d.addView(db,new LinearLayout.LayoutParams(-1,dp(46)));add(d);}
    private EditText field(String hint){EditText e=input(hint,false);add(e,5,0);return e;}
    private void restaurantForm(){if(!api.signedIn()){openLogin();return;}base("Restaurant / Hotel");add(tv("Partner application",23,INK),8,4);add(tv("Required information • Barmer service area",13,MUTED),0,12);EditText name=field("Restaurant / Hotel name *"),owner=field("Owner name *"),phone=field("Phone *"),addr=field("Address *"),area=field("Barmer area *"),reg=field("Registration / GST number (optional)");Button s=primary("Submit application");s.setOnClickListener(v->{JSONObject b=new JSONObject();try{b.put("applicant_id",api.userId());b.put("restaurant_name",name.getText().toString());b.put("owner_name",owner.getText().toString());b.put("phone",phone.getText().toString());b.put("address",addr.getText().toString());b.put("barmer_area",area.getText().toString());b.put("registration_no",reg.getText().toString());api.applyRestaurant(b,result("Application submitted"));}catch(Exception e){Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();}});add(s,12,0);}
    private void riderForm(){if(!api.signedIn()){openLogin();return;}base("Delivery Rider");add(tv("Rider application",23,INK),8,4);add(tv("We'll review your details before approval.",13,MUTED),0,12);EditText name=field("Full name *"),phone=field("Phone *"),addr=field("Address *"),vehicle=field("Vehicle number"),lic=field("Driving licence number"),em=field("Emergency contact");Button s=primary("Submit rider application");s.setOnClickListener(v->{JSONObject b=new JSONObject();try{b.put("applicant_id",api.userId());b.put("full_name",name.getText().toString());b.put("phone",phone.getText().toString());b.put("address",addr.getText().toString());b.put("vehicle_no",vehicle.getText().toString());b.put("licence_no",lic.getText().toString());b.put("emergency_contact",em.getText().toString());api.applyRider(b,result("Application submitted"));}catch(Exception e){Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();}});add(s,12,0);}

    private void empty(String icon,String title,String message,String action,Runnable go){LinearLayout box=card();box.setGravity(Gravity.CENTER);TextView i=tv(icon,42,PRIMARY);i.setGravity(Gravity.CENTER);box.addView(i,new LinearLayout.LayoutParams(-1,dp(60)));TextView t=tv(title,20,INK);t.setGravity(Gravity.CENTER);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);box.addView(t);TextView m=tv(message,13,MUTED);m.setGravity(Gravity.CENTER);m.setPadding(dp(10),dp(5),dp(10),dp(12));box.addView(m);Button b=primary(action);b.setOnClickListener(v->go.run());box.addView(b,new LinearLayout.LayoutParams(-1,dp(46)));add(box,28,0);}

    private void requestLocation(){if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},LOC);return;}try{LocationManager lm=(LocationManager)getSystemService(LOCATION_SERVICE);Location last=lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);if(last==null)last=lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);if(last!=null){lat=last.getLatitude();lng=last.getLongitude();Toast.makeText(this,"Location set ✓",Toast.LENGTH_SHORT).show();showHome();return;}lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,1,new LocationListener(){public void onLocationChanged(Location l){lat=l.getLatitude();lng=l.getLongitude();lm.removeUpdates(this);runOnUiThread(()->{Toast.makeText(MainActivity.this,"Location set ✓",Toast.LENGTH_SHORT).show();showHome();});}public void onProviderEnabled(String p){}public void onProviderDisabled(String p){}public void onStatusChanged(String p,int s,android.os.Bundle b){}});}catch(Exception e){Toast.makeText(this,"Location unavailable",Toast.LENGTH_LONG).show();}}
    @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);if(r==LOC&&g.length>0&&g[0]==PackageManager.PERMISSION_GRANTED)requestLocation();}
}

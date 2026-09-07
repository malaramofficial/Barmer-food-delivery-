package com.malaramofficial.barmerfooddelivery;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;

/** Secure native staff/admin entry point. Backend role remains authoritative. */
public final class AdminActivity extends Activity {
    private static final int BG=Color.rgb(247,247,245),INK=Color.rgb(25,25,28),MUTED=Color.rgb(105,105,110),PRIMARY=Color.rgb(232,83,37),GREEN=Color.rgb(30,137,77);
    private LinearLayout root,content; private NativeApi api;
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(Color.WHITE);getWindow().setNavigationBarColor(Color.WHITE);getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);api=new NativeApi(this);showLogin();}
    private int dp(int n){return(int)(n*getResources().getDisplayMetrics().density+.5f);}
    private TextView tv(String s,float size,int color){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);v.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));v.setIncludeFontPadding(true);return v;}
    private GradientDrawable shape(int color,float radius){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp((int)radius));return g;}
    private EditText input(String hint,boolean pass){EditText e=new EditText(this);e.setHint(hint);e.setTextSize(15);e.setSingleLine();e.setPadding(dp(16),0,dp(16),0);e.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));e.setBackground(shape(Color.WHITE,16));if(pass)e.setInputType(129);return e;}
    private Button button(String text){Button b=new Button(this);b.setText(text);b.setTextSize(15);b.setAllCaps(false);b.setTextColor(Color.WHITE);b.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));b.setMinHeight(dp(50));b.setBackground(shape(PRIMARY,16));return b;}
    private LinearLayout card(){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(16),dp(14),dp(16),dp(14));c.setBackground(shape(Color.WHITE,18));c.setElevation(dp(1));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(5),0,dp(5));c.setLayoutParams(p);return c;}
    private void showLogin(){
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(18),dp(10),dp(18),dp(20));root.setBackgroundColor(BG);setContentView(root);
        ScrollView sc=new ScrollView(this);sc.setFillViewport(true);LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);sc.addView(body);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
        TextView back=tv("‹  Back to customer app",16,INK);back.setPadding(0,dp(8),0,dp(18));back.setOnClickListener(v->finish());body.addView(back);
        LinearLayout hero=new LinearLayout(this);hero.setOrientation(LinearLayout.VERTICAL);hero.setPadding(dp(20),dp(22),dp(20),dp(22));hero.setBackground(shape(Color.rgb(38,38,42),24));
        TextView lock=tv("🔐",30,Color.WHITE);hero.addView(lock);TextView title=tv("Admin & Staff",27,Color.WHITE);title.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));hero.addView(title);hero.addView(tv("Secure operations access",14,Color.rgb(220,220,224)));body.addView(hero);
        EditText email=input("Admin email",false);LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(-1,-2);ep.setMargins(0,dp(18),0,0);body.addView(email,ep);
        EditText pass=input("Password",true);LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,-2);pp.setMargins(0,dp(10),0,0);body.addView(pass,pp);
        TextView note=tv("Only an account with the backend role = admin can enter the admin area.",13,MUTED);note.setPadding(dp(4),dp(12),dp(4),dp(18));body.addView(note);
        Button login=button("Sign in to Admin");login.setOnClickListener(v->{hideKeyboard();if(!api.configured()){toast("Backend is not configured in this build.");return;}String e=email.getText().toString().trim(),p=pass.getText().toString();if(e.isEmpty()||p.isEmpty()){toast("Email और password दोनों भरें");return;}login.setEnabled(false);api.signIn(e,p,new NativeApi.Callback(){public void ok(JSONObject d){verifyAdmin(login);}public void error(String m){runOnUiThread(()->{login.setEnabled(true);toast(m);});}});});body.addView(login,new LinearLayout.LayoutParams(-1,dp(52)));
        TextView help=tv("Customer accounts are never granted admin access.",12,MUTED);help.setGravity(Gravity.CENTER);help.setPadding(0,dp(18),0,dp(8));body.addView(help);
    }
    private void verifyAdmin(Button login){api.profile(new NativeApi.Callback(){public void ok(JSONObject d){runOnUiThread(()->{try{JSONArray a=d.optJSONArray("data");JSONObject p=a==null?null:a.optJSONObject(0);String role=p==null?"":p.optString("role","");if("admin".equalsIgnoreCase(role)){showDashboard();}else{api.signOut();login.setEnabled(true);toast("यह account admin नहीं है.");}}catch(Exception e){api.signOut();login.setEnabled(true);toast("Admin role verify नहीं हो सका");}});}public void error(String m){api.signOut();runOnUiThread(()->{login.setEnabled(true);toast("Admin role verify failed: "+m);});}});}
    private void showDashboard(){
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BG);root.setPadding(dp(18),dp(8),dp(18),0);setContentView(root);
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);TextView title=tv("Admin Control Center",22,INK);title.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));top.addView(title,new LinearLayout.LayoutParams(0,dp(54),1));TextView out=tv("Sign out",13,PRIMARY);out.setGravity(Gravity.CENTER);out.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));out.setOnClickListener(v->{api.signOut();finish();});top.addView(out,new LinearLayout.LayoutParams(dp(72),dp(48)));root.addView(top);
        ScrollView sc=new ScrollView(this);sc.setFillViewport(true);content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(0,dp(5),0,dp(28));sc.addView(content);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout hero=card();hero.setBackground(shape(Color.rgb(38,38,42),20));TextView h=tv("Operations overview",20,Color.WHITE);h.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));hero.addView(h);hero.addView(tv("Live data from the secure admin API",13,Color.rgb(215,215,220)));content.addView(hero);
        TextView loading=tv("Loading…",14,MUTED);content.addView(loading,new LinearLayout.LayoutParams(-1,dp(48)));
        api.adminDashboard(new NativeApi.Callback(){public void ok(JSONObject d){runOnUiThread(()->renderDashboard(d,loading));}public void error(String m){runOnUiThread(()->{loading.setText("Dashboard error: "+m);});}});
    }
    private void renderDashboard(JSONObject d,TextView loading){content.removeView(loading);LinearLayout metrics=new LinearLayout(this);metrics.setGravity(Gravity.CENTER);String[][] m={{"Users",count(d,"users")},{"Restaurants",count(d,"restaurants")},{"Orders",count(d,"orders")},{"Pending KYC/Apps",String.valueOf(count(d,"pending_restaurant_applications")+count(d,"pending_rider_applications"))}};for(String[] x:m){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setGravity(Gravity.CENTER);c.setPadding(dp(8),dp(12),dp(8),dp(12));c.setBackground(shape(Color.WHITE,18));TextView n=tv(x[1],23,PRIMARY);n.setGravity(Gravity.CENTER);n.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));c.addView(n);TextView l=tv(x[0],11,MUTED);l.setGravity(Gravity.CENTER);c.addView(l);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(82),1);p.setMargins(dp(3),dp(8),dp(3),dp(8));metrics.addView(c,p);}content.addView(metrics);
        section("Pending restaurant applications",d.optJSONArray("pending_restaurant_applications"));section("Pending rider applications",d.optJSONArray("pending_rider_applications"));section("Recent orders",d.optJSONArray("orders"));
        Button refresh=button("Refresh dashboard");refresh.setOnClickListener(v->showDashboard());refresh.setTextSize(14);content.addView(refresh,new LinearLayout.LayoutParams(-1,dp(48)));
    }
    private void section(String title,JSONArray a){TextView h=tv(title,17,INK);h.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,-2);hp.setMargins(0,dp(12),0,dp(4));content.addView(h,hp);if(a==null||a.length()==0){content.addView(tv("Nothing pending",13,MUTED));return;}for(int i=0;i<Math.min(a.length(),5);i++){JSONObject x=a.optJSONObject(i);if(x==null)continue;LinearLayout c=card();String name=x.optString("restaurant_name",x.optString("full_name",x.optString("id","Record")));c.addView(tv(name,15,INK));String status=x.optString("status",x.optString("created_at",""));c.addView(tv(status,12,MUTED));content.addView(c);}}
    private String count(JSONObject d,String key){JSONArray a=d.optJSONArray(key);return a==null?"0":String.valueOf(a.length());}
    private void hideKeyboard(){((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(root.getWindowToken(),0);}
    private void toast(String s){runOnUiThread(()->Toast.makeText(this,s,Toast.LENGTH_LONG).show());}
}

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
import com.google.firebase.auth.FirebaseUser;
import org.json.JSONArray;
import org.json.JSONObject;

/** Clean native operations portal. Firebase owns identity; backend role remains authoritative. */
public final class AdminActivity extends Activity {
    private static final int BG=Color.rgb(247,247,245),INK=Color.rgb(25,26,28),MUTED=Color.rgb(104,105,109),PRIMARY=Color.rgb(225,78,32),DARK=Color.rgb(31,32,35),LINE=Color.rgb(228,226,222);
    private LinearLayout root,content;private NativeApi api;private FirebaseAuthManager firebase;
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(Color.WHITE);getWindow().setNavigationBarColor(Color.WHITE);getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);api=new NativeApi(this);firebase=new FirebaseAuthManager(this);showLogin();}
    private int dp(int n){return(int)(n*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable bg(int c,float r){GradientDrawable g=new GradientDrawable();g.setColor(c);g.setCornerRadius(dp((int)r));return g;}
    private GradientDrawable outline(int c,int s,float r){GradientDrawable g=bg(Color.WHITE,r);g.setStroke(dp(s),c);return g;}
    private TextView tv(String s,float size,int c){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(c);v.setIncludeFontPadding(true);return v;}
    private void bold(TextView v,float size){v.setTextSize(size);v.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));}
    private EditText field(String hint,boolean pass){EditText e=new EditText(this);e.setHint(hint);e.setTextSize(15);e.setTextColor(INK);e.setHintTextColor(MUTED);e.setSingleLine(true);e.setPadding(dp(15),0,dp(15),0);e.setBackground(outline(LINE,1,14));e.setMinHeight(dp(52));if(pass)e.setInputType(129);return e;}
    private Button button(String s){Button b=new Button(this);b.setText(s);b.setTextSize(15);b.setAllCaps(false);b.setTextColor(Color.WHITE);b.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));b.setMinHeight(dp(52));b.setBackground(bg(PRIMARY,14));return b;}
    private LinearLayout card(){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(16),dp(15),dp(16),dp(15));c.setBackground(bg(Color.WHITE,18));c.setElevation(dp(1));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(5),0,dp(5));c.setLayoutParams(p);return c;}

    private void showLogin(){
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BG);root.setPadding(dp(20),dp(10),dp(20),0);setContentView(root);
        ScrollView sc=new ScrollView(this);sc.setFillViewport(true);LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(0,0,0,dp(26));sc.addView(body);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
        TextView back=tv("‹  Customer app",15,INK);back.setPadding(0,dp(7),0,dp(17));back.setOnClickListener(v->finish());body.addView(back);
        LinearLayout hero=new LinearLayout(this);hero.setOrientation(LinearLayout.VERTICAL);hero.setPadding(dp(20),dp(20),dp(20),dp(20));hero.setBackground(bg(DARK,20));TextView eyebrow=tv("BARMER FOOD • OPERATIONS",11,Color.rgb(205,205,210));bold(eyebrow,11);hero.addView(eyebrow);TextView title=tv("Staff portal",27,Color.WHITE);bold(title,27);title.setPadding(0,dp(5),0,0);hero.addView(title);hero.addView(tv("Firebase-secured access for administration and operations.",13,Color.rgb(220,220,224)));body.addView(hero);
        TextView label=tv("ADMIN ACCOUNT",11,MUTED);bold(label,11);label.setPadding(dp(2),dp(20),0,dp(7));body.addView(label);
        EditText email=field("Admin email",false);body.addView(email,new LinearLayout.LayoutParams(-1,dp(52)));EditText pass=field("Password",true);LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,dp(52));pp.setMargins(0,dp(9),0,0);body.addView(pass,pp);
        TextView note=tv("Identity comes from Firebase. Only a backend profile with role = admin can enter this area.",12,MUTED);note.setPadding(dp(2),dp(11),dp(2),dp(14));body.addView(note);
        Button login=button("Sign in");body.addView(login,new LinearLayout.LayoutParams(-1,dp(52)));login.setOnClickListener(v->{hideKeyboard();if(!api.configured()||!firebase.configured()){toast("Firebase / backend configuration incomplete.");return;}String e=email.getText().toString().trim(),p=pass.getText().toString();if(e.isEmpty()||p.isEmpty()){toast("Email और password दोनों भरें");return;}login.setEnabled(false);firebase.signInWithEmailPassword(e,p,new FirebaseAuthManager.Callback(){public void ok(String token){FirebaseUser u=firebase.currentUser();api.setFirebaseSession(token,u==null?"":u.getUid());api.ensureFirebaseProfile(u==null?"Admin":(u.getDisplayName()==null?"Admin":u.getDisplayName()),u==null?"":(u.getPhoneNumber()==null?"":u.getPhoneNumber()),new NativeApi.Callback(){public void ok(JSONObject d){verifyAdmin(login);}public void error(String m){firebase.signOut();login.setEnabled(true);toast("Profile setup failed: "+m);}});}public void error(String m){runOnUiThread(()->{login.setEnabled(true);toast(m);});}});});
        LinearLayout safe= new LinearLayout(this);safe.setGravity(Gravity.CENTER_VERTICAL);safe.setPadding(dp(13),dp(12),dp(13),dp(12));safe.setBackground(bg(Color.WHITE,16));TextView shield=tv("✓",18,Color.rgb(28,132,74));bold(shield,18);safe.addView(shield,new LinearLayout.LayoutParams(dp(32),dp(32)));safe.addView(tv("Customer accounts never receive admin access.",11,MUTED),new LinearLayout.LayoutParams(0,-2,1));LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2);sp.setMargins(0,dp(14),0,0);body.addView(safe,sp);
    }
    private void verifyAdmin(Button login){api.profile(new NativeApi.Callback(){public void ok(JSONObject d){runOnUiThread(()->{try{JSONArray a=d.optJSONArray("data");JSONObject p=a==null?null:a.optJSONObject(0);String role=p==null?"":p.optString("role","");if("admin".equalsIgnoreCase(role)){showDashboard();}else{api.signOut();login.setEnabled(true);toast("यह account admin नहीं है.");}}catch(Exception e){api.signOut();login.setEnabled(true);toast("Admin role verify नहीं हो सका");}});}public void error(String m){api.signOut();runOnUiThread(()->{login.setEnabled(true);toast("Admin role verify failed: "+m);});}});}
    private void showDashboard(){
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BG);root.setPadding(dp(20),dp(8),dp(20),0);setContentView(root);
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);LinearLayout copy=new LinearLayout(this);copy.setOrientation(LinearLayout.VERTICAL);TextView k=tv("BARMER FOOD",10,MUTED);bold(k,10);copy.addView(k);TextView title=tv("Control center",21,INK);bold(title,21);copy.addView(title);top.addView(copy,new LinearLayout.LayoutParams(0,dp(58),1));TextView out=tv("Sign out",12,PRIMARY);bold(out,12);out.setGravity(Gravity.CENTER);out.setOnClickListener(v->{api.signOut();finish();});top.addView(out,new LinearLayout.LayoutParams(dp(72),dp(44)));root.addView(top);
        ScrollView sc=new ScrollView(this);sc.setFillViewport(true);content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(0,0,0,dp(28));sc.addView(content);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout hero=card();hero.setBackground(bg(DARK,18));TextView h=tv("Today’s operations",19,Color.WHITE);bold(h,19);hero.addView(h);hero.addView(tv("Live overview from the secure admin API",12,Color.rgb(215,215,220)));content.addView(hero);
        TextView loading=tv("Loading dashboard…",13,MUTED);loading.setPadding(dp(2),dp(18),0,dp(18));content.addView(loading);
        api.adminDashboard(new NativeApi.Callback(){public void ok(JSONObject d){runOnUiThread(()->renderDashboard(d,loading));}public void error(String m){runOnUiThread(()->loading.setText("Dashboard unavailable. "+m));}});
    }
    private void renderDashboard(JSONObject d,TextView loading){content.removeView(loading);LinearLayout metrics=new LinearLayout(this);String[][] m={{"Users",count(d,"users")},{"Restaurants",count(d,"restaurants")},{"Orders",count(d,"orders")},{"Pending",String.valueOf(count(d,"pending_restaurant_applications")+count(d,"pending_rider_applications"))}};for(int i=0;i<m.length;i++){LinearLayout c=card();c.setGravity(Gravity.CENTER);TextView n=tv(m[i][1],24,PRIMARY);bold(n,24);n.setGravity(Gravity.CENTER);c.addView(n);TextView l=tv(m[i][0],11,MUTED);l.setGravity(Gravity.CENTER);c.addView(l);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(78),1);p.setMargins(i==0?0:dp(5),0,0,0);metrics.addView(c,p);}content.addView(metrics);section("Restaurant applications",d.optJSONArray("pending_restaurant_applications"));section("Rider applications",d.optJSONArray("pending_rider_applications"));section("Recent orders",d.optJSONArray("orders"));Button refresh=button("Refresh dashboard");refresh.setOnClickListener(v->showDashboard());LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(50));rp.setMargins(0,dp(14),0,0);content.addView(refresh,rp);}
    private void section(String title,JSONArray a){TextView h=tv(title,17,INK);bold(h,17);LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,-2);hp.setMargins(0,dp(17),0,dp(4));content.addView(h,hp);if(a==null||a.length()==0){TextView empty=tv("Nothing to review",12,MUTED);empty.setPadding(0,dp(4),0,dp(4));content.addView(empty);return;}for(int i=0;i<Math.min(a.length(),5);i++){JSONObject x=a.optJSONObject(i);if(x==null)continue;LinearLayout c=card();TextView n=tv(x.optString("restaurant_name",x.optString("full_name",x.optString("id","Record"))),14,INK);bold(n,14);c.addView(n);c.addView(tv(x.optString("status",x.optString("created_at","")),11,MUTED));content.addView(c);}}
    private String count(JSONObject d,String key){JSONArray a=d.optJSONArray(key);return a==null?"0":String.valueOf(a.length());}
    private void hideKeyboard(){((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(root.getWindowToken(),0);}
    private void toast(String s){runOnUiThread(()->Toast.makeText(this,s,Toast.LENGTH_LONG).show());}
}

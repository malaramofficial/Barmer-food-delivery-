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
    private static final int BG = Color.rgb(247,247,245);
    private static final int INK = Color.rgb(25,25,28);
    private static final int MUTED = Color.rgb(105,105,110);
    private static final int PRIMARY = Color.rgb(232,83,37);
    private LinearLayout root;
    private NativeApi api;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        api = new NativeApi(this);
        showLogin();
    }

    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }
    private TextView tv(String s,float size,int color){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);return v;}
    private GradientDrawable shape(int color,float radius){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp((int)radius));return g;}
    private EditText input(String hint,boolean pass){EditText e=new EditText(this);e.setHint(hint);e.setTextSize(15);e.setSingleLine();e.setPadding(dp(16),0,dp(16),0);e.setBackground(shape(Color.WHITE,16));if(pass)e.setInputType(129);return e;}
    private Button button(String text){Button b=new Button(this);b.setText(text);b.setTextSize(15);b.setAllCaps(false);b.setTextColor(Color.WHITE);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setMinHeight(dp(52));b.setBackground(shape(PRIMARY,16));return b;}

    private void showLogin(){
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(22),dp(16),dp(22),dp(22));root.setBackgroundColor(BG);setContentView(root);
        TextView back=tv("‹  Back to customer app",16,INK);back.setPadding(0,dp(8),0,dp(20));back.setOnClickListener(v->finish());root.addView(back);
        LinearLayout hero=new LinearLayout(this);hero.setOrientation(LinearLayout.VERTICAL);hero.setPadding(dp(20),dp(22),dp(20),dp(22));hero.setBackground(shape(Color.rgb(38,38,42),24));
        TextView lock=tv("🔐",30,Color.WHITE);hero.addView(lock);TextView title=tv("Admin & Staff",27,Color.WHITE);title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);hero.addView(title);hero.addView(tv("Secure operations access",14,Color.rgb(220,220,224)));root.addView(hero,new LinearLayout.LayoutParams(-1,-2));
        Space s=new Space(this);root.addView(s,new LinearLayout.LayoutParams(1,dp(22)));
        EditText email=input("Admin email",false);root.addView(email,new LinearLayout.LayoutParams(-1,dp(54)));
        EditText pass=input("Password",true);LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,dp(54));pp.setMargins(0,dp(10),0,0);root.addView(pass,pp);
        TextView note=tv("Only an account with the backend role = admin can enter the admin area.",13,MUTED);note.setPadding(dp(4),dp(12),dp(4),dp(18));root.addView(note);
        Button login=button("Sign in to Admin");login.setOnClickListener(v->{hideKeyboard();if(!api.configured()){toast("Supabase configuration missing — this build has no backend URL/key.");return;}String e=email.getText().toString().trim(),p=pass.getText().toString();if(e.isEmpty()||p.isEmpty()){toast("Email और password दोनों भरें");return;}login.setEnabled(false);api.signIn(e,p,new NativeApi.Callback(){public void ok(JSONObject d){verifyAdmin(login);}public void error(String m){runOnUiThread(()->{login.setEnabled(true);toast(m);});}});});root.addView(login,new LinearLayout.LayoutParams(-1,dp(52)));
        TextView help=tv("Customer accounts are not granted admin access.",12,MUTED);help.setGravity(Gravity.CENTER);help.setPadding(0,dp(18),0,0);root.addView(help);
    }
    private void verifyAdmin(Button login){api.profile(new NativeApi.Callback(){public void ok(JSONObject d){runOnUiThread(()->{try{JSONArray a=d.optJSONArray("data");JSONObject p=a==null?null:a.optJSONObject(0);String role=p==null?"":p.optString("role","");if("admin".equalsIgnoreCase(role)){toast("Admin login successful ✓");finish();}else{api.signOut();login.setEnabled(true);toast("यह account admin नहीं है.");}}catch(Exception e){api.signOut();login.setEnabled(true);toast("Admin role verify नहीं हो सका");}});}public void error(String m){api.signOut();runOnUiThread(()->{login.setEnabled(true);toast("Admin role verify failed: "+m);});}});}
    private void hideKeyboard(){((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(root.getWindowToken(),0);}
    private void toast(String s){runOnUiThread(()->Toast.makeText(this,s,Toast.LENGTH_LONG).show());}
}

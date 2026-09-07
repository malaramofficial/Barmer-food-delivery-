package com.malaramofficial.barmerfooddelivery;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import com.google.firebase.auth.FirebaseUser;
import org.json.JSONObject;

/** Native customer entry point. Firebase Google Sign-In is the only customer authentication method. */
public final class LoginActivity extends Activity {
    private static final int PRIMARY=Color.rgb(225,78,32), INK=Color.rgb(28,29,31), MUTED=Color.rgb(105,106,110), BG=Color.rgb(247,247,245), LINE=Color.rgb(228,226,222), SOFT=Color.rgb(255,242,236);
    private NativeApi api; private FirebaseAuthManager firebase; private Button google;
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(Color.WHITE);getWindow().setNavigationBarColor(Color.WHITE);getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);api=new NativeApi(this);firebase=new FirebaseAuthManager(this);build();}
    private int dp(int n){return(int)(n*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable bg(int c,float r){GradientDrawable g=new GradientDrawable();g.setColor(c);g.setCornerRadius(dp((int)r));return g;}
    private GradientDrawable outline(int c,int s,float r){GradientDrawable g=bg(Color.WHITE,r);g.setStroke(dp(s),c);return g;}
    private TextView tv(String s,float size,int c){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(c);v.setIncludeFontPadding(true);return v;}
    private void bold(TextView v,float size){v.setTextSize(size);v.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));}
    private Button button(String s){Button b=new Button(this);b.setText(s);b.setTextSize(15);b.setAllCaps(false);b.setTextColor(INK);b.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));b.setMinHeight(dp(54));b.setBackground(outline(LINE,1,15));return b;}
    private void build(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BG);setContentView(root);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(20),dp(12),dp(20),dp(28));scroll.addView(body);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);TextView back=tv("‹",32,INK);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->finish());top.addView(back,new LinearLayout.LayoutParams(dp(44),dp(46)));TextView brand=tv("Barmer Food",16,INK);bold(brand,16);top.addView(brand,new LinearLayout.LayoutParams(0,dp(46),1));TextView help=tv("Help",12,MUTED);help.setGravity(Gravity.CENTER);top.addView(help,new LinearLayout.LayoutParams(dp(44),dp(46)));body.addView(top);
        LinearLayout intro=new LinearLayout(this);intro.setOrientation(LinearLayout.VERTICAL);intro.setPadding(0,dp(34),0,dp(20));TextView title=tv("खाने का सफ़र शुरू करें",29,INK);bold(title,29);intro.addView(title);TextView sub=tv("अपने Google account से सुरक्षित रूप से जारी रखें।",14,MUTED);sub.setPadding(0,dp(8),0,0);intro.addView(sub);body.addView(intro);
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(18),dp(18),dp(18),dp(18));card.setBackground(bg(Color.WHITE,20));card.setElevation(dp(1));TextView ct=tv("Customer sign-in",17,INK);bold(ct,17);card.addView(ct);TextView cd=tv("एक ही tap में Firebase-secured Google login",12,MUTED);cd.setPadding(0,dp(5),0,dp(16));card.addView(cd);
        google=button("  Continue with Google  ");card.addView(google,new LinearLayout.LayoutParams(-1,dp(54)));google.setOnClickListener(v->startGoogle());body.addView(card,new LinearLayout.LayoutParams(-1,-2));
        LinearLayout staff=new LinearLayout(this);staff.setGravity(Gravity.CENTER_VERTICAL);staff.setPadding(dp(14),dp(12),dp(12),dp(12));staff.setBackground(bg(SOFT,16));LinearLayout copy=new LinearLayout(this);copy.setOrientation(LinearLayout.VERTICAL);TextView st=tv("Restaurant / Rider / Admin",13,INK);bold(st,13);copy.addView(st);copy.addView(tv("Staff के लिए अलग secure portal",11,MUTED));staff.addView(copy,new LinearLayout.LayoutParams(0,-2,1));TextView admin=tv("Admin login  →",12,PRIMARY);bold(admin,12);admin.setGravity(Gravity.CENTER);admin.setOnClickListener(v->startActivity(new Intent(this,AdminActivity.class)));staff.addView(admin,new LinearLayout.LayoutParams(dp(108),dp(42)));LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2);sp.setMargins(0,dp(18),0,0);body.addView(staff,sp);
        TextView guest=tv("Browse restaurants without signing in  →",13,MUTED);guest.setGravity(Gravity.CENTER);guest.setPadding(0,dp(22),0,dp(8));guest.setOnClickListener(v->openHome());body.addView(guest);
        if(!firebase.configured()||!api.configured()){TextView warn=tv("Google Sign-In setup या backend configuration अभी पूरी नहीं है।",12,Color.rgb(150,92,20));warn.setGravity(Gravity.CENTER);warn.setPadding(dp(6),dp(8),dp(6),0);body.addView(warn);google.setEnabled(false);}
    }
    private void startGoogle(){if(!firebase.configured()){showError("Firebase Auth configure नहीं है।");return;}google.setEnabled(false);new GoogleAuth(this).signIn(this,api,new GoogleAuth.Callback(){public void ok(){ensureProfile();}public void error(String m){google.setEnabled(true);showError(m);}});}
    private void ensureProfile(){FirebaseUser u=firebase.currentUser();String name=u!=null&&u.getDisplayName()!=null?u.getDisplayName():"Customer";String phone=u!=null&&u.getPhoneNumber()!=null?u.getPhoneNumber():"";api.ensureFirebaseProfile(name,phone,new NativeApi.Callback(){public void ok(JSONObject d){runOnUiThread(()->openHome());}public void error(String m){runOnUiThread(()->{google.setEnabled(true);showError("Profile setup failed: "+m);});}});}
    private void openHome(){Intent i=new Intent(this,MainActivity.class);i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);startActivity(i);finish();}
    private void showError(String m){Toast.makeText(this,m==null||m.isEmpty()?"Something went wrong":m,Toast.LENGTH_LONG).show();}
}

package com.malaramofficial.barmerfooddelivery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.google.firebase.auth.FirebaseUser;
import org.json.JSONObject;

/** Clean native authentication experience. Firebase is the only identity provider. */
public final class LoginActivity extends Activity {
    private static final int PRIMARY=Color.rgb(225,78,32), INK=Color.rgb(28,29,31), MUTED=Color.rgb(105,106,110), BG=Color.rgb(247,247,245), LINE=Color.rgb(228,226,222), SOFT=Color.rgb(255,242,236);
    private LinearLayout body, fields; private NativeApi api; private FirebaseAuthManager firebase; private Button action; private TextView loginTab, signupTab;
    private EditText email, password, name, phone;
    private boolean signupMode=false;

    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(Color.WHITE);getWindow().setNavigationBarColor(Color.WHITE);getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);api=new NativeApi(this);firebase=new FirebaseAuthManager(this);build();}
    private int dp(int n){return(int)(n*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable bg(int c,float r){GradientDrawable g=new GradientDrawable();g.setColor(c);g.setCornerRadius(dp((int)r));return g;}
    private GradientDrawable outline(int c,int s,float r){GradientDrawable g=bg(Color.WHITE,r);g.setStroke(dp(s),c);return g;}
    private TextView tv(String s,float size,int c){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(c);v.setIncludeFontPadding(true);return v;}
    private void bold(TextView v,float size){v.setTextSize(size);v.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));}
    private EditText field(String hint,int type){EditText e=new EditText(this);e.setHint(hint);e.setTextSize(15);e.setTextColor(INK);e.setHintTextColor(MUTED);e.setSingleLine(true);e.setInputType(type);e.setPadding(dp(15),0,dp(15),0);e.setBackground(outline(LINE,1,14));e.setMinHeight(dp(52));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(52));p.setMargins(0,dp(9),0,0);fields.addView(e,p);return e;}
    private Button btn(String s,int fill,int color){Button b=new Button(this);b.setText(s);b.setTextSize(15);b.setAllCaps(false);b.setTextColor(color);b.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));b.setMinHeight(dp(52));b.setBackground(bg(fill,14));return b;}

    private void build(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BG);
        ScrollView sc=new ScrollView(this);sc.setFillViewport(true);body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(20),dp(12),dp(20),dp(28));sc.addView(body);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);TextView back=tv("‹",32,INK);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->finish());top.addView(back,new LinearLayout.LayoutParams(dp(44),dp(46)));TextView brand=tv("Barmer Food",16,INK);bold(brand,16);top.addView(brand,new LinearLayout.LayoutParams(0,dp(46),1));TextView help=tv("Help",12,MUTED);help.setGravity(Gravity.CENTER);top.addView(help,new LinearLayout.LayoutParams(dp(44),dp(46)));body.addView(top);
        LinearLayout intro=new LinearLayout(this);intro.setOrientation(LinearLayout.VERTICAL);intro.setPadding(0,dp(20),0,dp(17));TextView title=tv("खाने का सफ़र यहीं से शुरू करें",28,INK);bold(title,28);intro.addView(title);TextView sub=tv("Firebase से सुरक्षित login करें, orders track करें और checkout तेज़ रखें।",14,MUTED);sub.setPadding(0,dp(7),0,0);intro.addView(sub);body.addView(intro);
        LinearLayout tabs=new LinearLayout(this);tabs.setPadding(dp(4),dp(4),dp(4),dp(4));tabs.setBackground(bg(Color.rgb(238,237,234),14));loginTab=tv("Login",13,PRIMARY);signupTab=tv("Create account",13,MUTED);for(TextView t:new TextView[]{loginTab,signupTab}){t.setGravity(Gravity.CENTER);t.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));}tabs.addView(loginTab,new LinearLayout.LayoutParams(0,dp(42),1));tabs.addView(signupTab,new LinearLayout.LayoutParams(0,dp(42),1));body.addView(tabs,new LinearLayout.LayoutParams(-1,dp(50)));loginTab.setOnClickListener(v->setMode(false));signupTab.setOnClickListener(v->setMode(true));
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(16),dp(16),dp(16),dp(16));card.setBackground(bg(Color.WHITE,18));card.setElevation(dp(1));LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.setMargins(0,dp(14),0,0);body.addView(card,cp);
        fields=new LinearLayout(this);fields.setOrientation(LinearLayout.VERTICAL);card.addView(fields);
        email=field("Email address",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);password=field("Password",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);name=field("Full name",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PERSON_NAME);phone=field("Mobile number",InputType.TYPE_CLASS_PHONE);
        action=btn("Login",PRIMARY,Color.WHITE);LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(-1,dp(52));ap.setMargins(0,dp(14),0,0);card.addView(action,ap);action.setOnClickListener(v->submit());
        TextView divider=tv("OR",11,MUTED);divider.setGravity(Gravity.CENTER);LinearLayout.LayoutParams dp1=new LinearLayout.LayoutParams(-1,dp(38));dp1.setMargins(0,dp(6),0,0);card.addView(divider,dp1);
        Button google=btn("Continue with Google",Color.WHITE,INK);google.setBackground(outline(LINE,1,14));card.addView(google,new LinearLayout.LayoutParams(-1,dp(52)));google.setOnClickListener(v->{if(!firebase.configured()){showError("Firebase Auth configure नहीं है।");return;}google.setEnabled(false);new GoogleAuth(this).signIn(this,api,new GoogleAuth.Callback(){public void ok(){ensureProfile("Google login successful");}public void error(String m){google.setEnabled(true);showError(m);}});});
        LinearLayout staff=new LinearLayout(this);staff.setGravity(Gravity.CENTER_VERTICAL);staff.setPadding(dp(14),dp(12),dp(12),dp(12));staff.setBackground(bg(SOFT,16));LinearLayout copy=new LinearLayout(this);copy.setOrientation(LinearLayout.VERTICAL);TextView st=tv("Restaurant / Rider / Admin",13,INK);bold(st,13);copy.addView(st);copy.addView(tv("Staff के लिए secure portal",11,MUTED));staff.addView(copy,new LinearLayout.LayoutParams(0,-2,1));TextView admin=tv("Admin login  →",12,PRIMARY);bold(admin,12);admin.setGravity(Gravity.CENTER);admin.setOnClickListener(v->startActivity(new Intent(this,AdminActivity.class)));staff.addView(admin,new LinearLayout.LayoutParams(dp(108),dp(42)));LinearLayout.LayoutParams stp=new LinearLayout.LayoutParams(-1,-2);stp.setMargins(0,dp(16),0,0);body.addView(staff,stp);
        TextView guest=tv("Browse restaurants without an account  →",13,MUTED);guest.setGravity(Gravity.CENTER);guest.setPadding(0,dp(18),0,dp(8));guest.setOnClickListener(v->openHome(""));body.addView(guest);
        if(!api.configured()||!firebase.configured()){TextView warn=tv("Firebase login और Supabase data backend configuration पूरी होने तक account actions disabled हैं।",12,Color.rgb(150,92,20));warn.setGravity(Gravity.CENTER);warn.setPadding(dp(6),dp(8),dp(6),0);body.addView(warn);action.setEnabled(false);google.setEnabled(false);}
        setMode(false);
    }

    private void setMode(boolean signup){signupMode=signup;name.setVisibility(signup?View.VISIBLE:View.GONE);phone.setVisibility(signup?View.VISIBLE:View.GONE);action.setText(signup?"Create account":"Login");loginTab.setTextColor(signup?MUTED:PRIMARY);signupTab.setTextColor(signup?PRIMARY:MUTED);loginTab.setBackground(signup?null:bg(Color.WHITE,11));signupTab.setBackground(signup?bg(Color.WHITE,11):null);}

    private void submit(){
        hideKeyboard();
        String e=email.getText().toString().trim(),p=password.getText().toString();
        if(e.isEmpty()||p.isEmpty()){showError("Email और password दोनों भरें");return;}
        if(!firebase.configured()){showError("Firebase Auth configure नहीं है।");return;}
        action.setEnabled(false);
        FirebaseAuthManager.Callback cb=new FirebaseAuthManager.Callback(){
            public void ok(String token){
                FirebaseUser u=firebase.currentUser();
                api.setFirebaseSession(token,u==null?"":u.getUid());
                ensureProfile(signupMode?"Account created":"Login successful");
            }
            public void error(String m){runOnUiThread(()->{action.setEnabled(true);showError(m);});}
        };
        if(signupMode) firebase.createUserWithEmailPassword(e,p,cb); else firebase.signInWithEmailPassword(e,p,cb);
    }

    private void ensureProfile(String success){
        FirebaseUser u=firebase.currentUser();
        String fullName=name.getText().toString().trim();
        String phoneValue=phone.getText().toString().trim();
        if(fullName.isEmpty()&&u!=null&&u.getDisplayName()!=null)fullName=u.getDisplayName();
        if(fullName.isEmpty())fullName="Customer";
        if(phoneValue.isEmpty()&&u!=null&&u.getPhoneNumber()!=null)phoneValue=u.getPhoneNumber();
        api.ensureFirebaseProfile(fullName,phoneValue,new NativeApi.Callback(){
            public void ok(JSONObject d){runOnUiThread(()->{action.setEnabled(true);openHome(success);});}
            public void error(String m){runOnUiThread(()->{action.setEnabled(true);showError("Profile setup failed: "+m);});}
        });
    }

    private void openHome(String msg){if(msg!=null&&!msg.isEmpty())Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();Intent i=new Intent(this,MainActivity.class);i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);startActivity(i);finish();}
    private void showError(String m){Toast.makeText(this,m==null||m.isEmpty()?"Something went wrong":m,Toast.LENGTH_LONG).show();}
    private void hideKeyboard(){((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(body.getWindowToken(),0);}
}

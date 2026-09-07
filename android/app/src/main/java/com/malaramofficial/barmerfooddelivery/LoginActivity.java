package com.malaramofficial.barmerfooddelivery;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;

/** Dedicated native authentication screen. Keeps login UX independent from the customer home UI. */
public class LoginActivity extends Activity {
    private static final int PRIMARY = Color.rgb(232, 83, 37);
    private static final int INK = Color.rgb(30, 30, 32);
    private static final int MUTED = Color.rgb(104, 104, 110);
    private static final int BG = Color.rgb(248, 247, 245);
    private static final int SOFT = Color.rgb(255, 239, 231);
    private static final int GREEN = Color.rgb(30, 137, 77);
    private NativeApi api;
    private LinearLayout content;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        api = new NativeApi(this);
        build();
    }

    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }
    private GradientDrawable bg(int color, float radius) {
        GradientDrawable g = new GradientDrawable(); g.setColor(color); g.setCornerRadius(dp((int)radius)); return g;
    }
    private GradientDrawable outline(int color, int stroke, float radius) {
        GradientDrawable g = bg(Color.WHITE, radius); g.setStroke(dp(stroke), color); return g;
    }
    private TextView text(String s, float size, int color) {
        TextView v = new TextView(this); v.setText(s); v.setTextSize(size); v.setTextColor(color); return v;
    }
    private EditText field(String hint, int type) {
        EditText e = new EditText(this);
        e.setHint(hint); e.setTextSize(15); e.setSingleLine(true); e.setPadding(dp(16), 0, dp(16), 0);
        e.setInputType(type); e.setBackground(outline(Color.rgb(224,222,218), 1, 16));
        e.setMinHeight(dp(54)); return e;
    }
    private Button button(String label, int fill, int color) {
        Button b = new Button(this); b.setText(label); b.setTextSize(15); b.setAllCaps(false); b.setTextColor(color);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD); b.setMinHeight(dp(54)); b.setPadding(dp(16), 0, dp(16), 0); b.setBackground(bg(fill, 17));
        return b;
    }
    private void add(View v, int top, int bottom) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2); p.setMargins(0, dp(top), 0, dp(bottom)); content.addView(v, p);
    }

    private void build() {
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.setClipToPadding(false); scroll.setBackgroundColor(BG);
        content = new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(dp(20), dp(18), dp(20), dp(30)); scroll.addView(content);
        setContentView(scroll);

        LinearLayout top = new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL);
        TextView back = text("‹", 34, INK); back.setGravity(Gravity.CENTER); back.setOnClickListener(v -> finish());
        top.addView(back, new LinearLayout.LayoutParams(dp(44), dp(48)));
        TextView brand = text("Barmer Food Delivery", 17, INK); brand.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        top.addView(brand, new LinearLayout.LayoutParams(0, dp(48), 1));
        TextView help = text("Help", 13, MUTED); help.setGravity(Gravity.CENTER); top.addView(help, new LinearLayout.LayoutParams(dp(48), dp(48)));
        content.addView(top);

        LinearLayout hero = new LinearLayout(this); hero.setOrientation(LinearLayout.VERTICAL); hero.setPadding(dp(20), dp(20), dp(20), dp(20)); hero.setBackground(bg(PRIMARY, 24));
        TextView badge = text("BARMER • FOOD • DELIVERY", 11, Color.WHITE); badge.setTypeface(Typeface.DEFAULT, Typeface.BOLD); hero.addView(badge);
        TextView title = text("खाना ऑर्डर करना\nबस आसान हो गया।", 27, Color.WHITE); title.setTypeface(Typeface.DEFAULT, Typeface.BOLD); addTo(hero, title, 8, 0);
        TextView sub = text("अपने account से जल्दी checkout करें, orders देखें और delivery track करें।", 14, Color.rgb(255,240,234)); addTo(hero, sub, 7, 0);
        add(hero, 10, 16);

        LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setPadding(dp(18), dp(18), dp(18), dp(18)); card.setBackground(bg(Color.WHITE, 22)); card.setElevation(dp(2));
        TextView heading = text("Welcome back", 24, INK); heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD); card.addView(heading);
        TextView note = text("Login करें या नया account बनाएँ।", 13, MUTED); addTo(card, note, 5, 12);

        EditText email = field("Email address", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        EditText pass = field("Password", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        EditText name = field("Full name (new account)", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        EditText phone = field("Mobile number (new account)", InputType.TYPE_CLASS_PHONE);
        card.addView(email); addTo(card, pass, 9, 0); addTo(card, name, 9, 0); addTo(card, phone, 9, 12);

        Button login = button("Login", PRIMARY, Color.WHITE);
        login.setOnClickListener(v -> { hideKeyboard(); api.signIn(email.getText().toString().trim(), pass.getText().toString(), result("Login successful")); });
        card.addView(login);

        Button signup = button("Create account", Color.WHITE, INK); signup.setBackground(outline(Color.rgb(224,222,218), 1, 17));
        signup.setOnClickListener(v -> { hideKeyboard(); api.signUp(email.getText().toString().trim(), pass.getText().toString(), name.getText().toString().trim(), phone.getText().toString().trim(), result("Account created")); });
        addTo(card, signup, 9, 0);

        TextView or = text("OR", 11, MUTED); or.setGravity(Gravity.CENTER); or.setTypeface(Typeface.DEFAULT, Typeface.BOLD); addTo(card, or, 15, 10);
        Button google = button("Continue with Google", Color.WHITE, INK); google.setBackground(outline(Color.rgb(210,208,204), 1, 17));
        google.setOnClickListener(v -> { if (!api.configured()) { showBackendError(); return; } new GoogleAuth(this).signIn(this, api, new GoogleAuth.Callback() { public void ok() { openHome("Google login successful"); } public void error(String m) { showError(m); } }); });
        card.addView(google);
        content.addView(card);

        LinearLayout admin = new LinearLayout(this); admin.setGravity(Gravity.CENTER_VERTICAL); admin.setPadding(dp(15), dp(13), dp(15), dp(13)); admin.setBackground(bg(SOFT, 17));
        LinearLayout adminCopy = new LinearLayout(this); adminCopy.setOrientation(LinearLayout.VERTICAL); TextView at = text("Restaurant / Rider / Admin", 13, INK); at.setTypeface(Typeface.DEFAULT, Typeface.BOLD); adminCopy.addView(at); TextView as = text("Staff portal के लिए अलग secure login", 11, MUTED); adminCopy.addView(as); admin.addView(adminCopy, new LinearLayout.LayoutParams(0, -2, 1));
        Button adminBtn = button("Admin login", Color.WHITE, PRIMARY); adminBtn.setTextSize(12); adminBtn.setMinHeight(dp(42)); admin.addView(adminBtn, new LinearLayout.LayoutParams(dp(112), dp(44)));
        adminBtn.setOnClickListener(v -> startActivity(new Intent(this, AdminActivity.class)));
        add(admin, 14, 0);

        TextView guest = text("Browse restaurants without logging in  ›", 13, MUTED); guest.setGravity(Gravity.CENTER); guest.setPadding(0, dp(18), 0, dp(4));
        guest.setOnClickListener(v -> openHome("")); content.addView(guest);

        if (!api.configured()) {
            TextView warning = text("Backend अभी configure नहीं है. GitHub Actions में SUPABASE_URL और SUPABASE_ANON_KEY secrets जोड़ने के बाद login/signup काम करेगा।", 12, GREEN);
            warning.setGravity(Gravity.CENTER); warning.setPadding(dp(8), dp(12), dp(8), 0); content.addView(warning);
            login.setEnabled(false); signup.setEnabled(false);
        }
    }

    private void addTo(LinearLayout parent, View child, int top, int bottom) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2); p.setMargins(0, dp(top), 0, dp(bottom)); parent.addView(child, p);
    }
    private NativeApi.Callback result(final String success) {
        return new NativeApi.Callback() {
            public void ok(JSONObject r) { runOnUiThread(() -> openHome(success)); }
            public void error(String m) { runOnUiThread(() -> showError(m)); }
        };
    }
    private void openHome(String message) {
        if (message != null && !message.isEmpty()) Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, MainActivity.class); i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); startActivity(i); finish();
    }
    private void showBackendError() { showError(api.configurationStatus()); }
    private void showError(String m) { Toast.makeText(this, m == null || m.isEmpty() ? "Something went wrong" : m, Toast.LENGTH_LONG).show(); }
    private void hideKeyboard() { ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(content.getWindowToken(), 0); }
}

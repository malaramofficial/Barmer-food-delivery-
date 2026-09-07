package com.malaramofficial.barmerfooddelivery;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/** Native authentication shortcuts plus a device-safe typography pass. */
public final class BfdApplication extends Application {
    @Override public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override public void onActivityResumed(Activity activity) {
                polishTree(activity.getWindow().getDecorView());
                attachAuthActions(activity);
            }
            @Override public void onActivityCreated(Activity a, Bundle b) {}
            @Override public void onActivityStarted(Activity a) {}
            @Override public void onActivityPaused(Activity a) {}
            @Override public void onActivityStopped(Activity a) {}
            @Override public void onActivitySaveInstanceState(Activity a, Bundle b) {}
            @Override public void onActivityDestroyed(Activity a) {}
        });
    }

    private void attachAuthActions(Activity activity) {
        if (!(activity instanceof MainActivity)) return;
        View welcome = findText(activity.getWindow().getDecorView(), "Welcome back 👋");
        if (!(welcome instanceof TextView) || !(welcome.getParent() instanceof LinearLayout)) return;
        LinearLayout parent = (LinearLayout) welcome.getParent();
        if (!findButton(parent, "Google")) addGoogle(activity, parent, welcome);
        if (!findButton(parent, "Admin / Staff Login")) addAdmin(activity, parent);
    }

    private void addGoogle(Activity activity, LinearLayout parent, View welcome) {
        Button google = new Button(activity);
        google.setText("G  Continue with Google"); google.setTextSize(15); google.setAllCaps(false);
        google.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL)); google.setTextColor(Color.rgb(35,35,38));
        google.setMinHeight(dp(activity, 50)); google.setPadding(dp(activity, 12), 0, dp(activity, 12), 0);
        google.setBackground(round(Color.WHITE, dp(activity, 14))); google.setElevation(dp(activity, 1));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(activity, 8), 0, dp(activity, 8)); google.setLayoutParams(lp);
        google.setOnClickListener(v -> {
            google.setEnabled(false);
            new GoogleAuth(activity).signIn(activity, new NativeApi(activity), new GoogleAuth.Callback() {
                @Override public void ok() { activity.runOnUiThread(() -> { Toast.makeText(activity, "Google से sign-in सफल", Toast.LENGTH_SHORT).show(); activity.recreate(); }); }
                @Override public void error(String message) { activity.runOnUiThread(() -> { google.setEnabled(true); Toast.makeText(activity, message, Toast.LENGTH_LONG).show(); }); }
            });
        });
        int index = parent.indexOfChild(welcome) + 1;
        parent.addView(google, index);
    }

    private void addAdmin(Activity activity, LinearLayout parent) {
        TextView admin = new TextView(activity);
        admin.setText("Admin / Staff Login"); admin.setTextSize(13); admin.setTextColor(Color.rgb(100,100,105));
        admin.setGravity(Gravity.CENTER); admin.setPadding(0, dp(activity, 14), 0, dp(activity, 12));
        admin.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL)); admin.setContentDescription("Admin / Staff Login");
        admin.setOnClickListener(v -> activity.startActivity(new Intent(activity, AdminActivity.class)));
        parent.addView(admin);
    }

    /** Keep the native UI legible and non-clipping across OEM fonts and display sizes. */
    private void polishTree(View view) {
        if (view instanceof TextView) {
            TextView t = (TextView) view;
            if (!(t instanceof EditText)) {
                boolean bold = t.getTypeface() != null && t.getTypeface().isBold();
                t.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
                t.setIncludeFontPadding(true);
                t.setHorizontallyScrolling(false);
                if (t.getMaxLines() == Integer.MAX_VALUE) t.setMaxLines(4);
            } else {
                EditText e = (EditText) t;
                e.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
                e.setHorizontallyScrolling(false);
                e.setSingleLine(e.getInputType() != 0 && e.getMaxLines() == 1);
            }
        }
        if (view instanceof Button) {
            Button b = (Button) view;
            b.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            b.setAllCaps(false);
            b.setMinHeight(dp(view.getContext(), 48));
        }
        if (view instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) view;
            for (int i = 0; i < g.getChildCount(); i++) polishTree(g.getChildAt(i));
        }
    }

    private boolean findButton(LinearLayout parent, String text) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View v = parent.getChildAt(i);
            if (v instanceof Button && ((Button) v).getText().toString().contains(text)) return true;
            if (v instanceof TextView && ((TextView) v).getText().toString().contains(text)) return true;
        }
        return false;
    }

    private View findText(View root, String wanted) {
        if (root instanceof TextView && wanted.contentEquals(((TextView) root).getText())) return root;
        if (root instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) root;
            for (int i = 0; i < g.getChildCount(); i++) {
                View found = findText(g.getChildAt(i), wanted);
                if (found != null) return found;
            }
        }
        return null;
    }

    private int dp(Activity a, int n) { return (int)(n * a.getResources().getDisplayMetrics().density + .5f); }
    private GradientDrawable round(int color, float radius) { GradientDrawable g = new GradientDrawable(); g.setColor(color); g.setCornerRadius(radius); return g; }
}

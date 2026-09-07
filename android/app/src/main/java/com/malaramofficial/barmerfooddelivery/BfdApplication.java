package com.malaramofficial.barmerfooddelivery;

import android.app.Activity;
import android.app.Application;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/** Adds the Google one-tap action to the existing native login screen without WebView. */
public final class BfdApplication extends Application {
    @Override public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override public void onActivityResumed(Activity activity) { attachGoogle(activity); }
            @Override public void onActivityCreated(Activity a, Bundle b) {}
            @Override public void onActivityStarted(Activity a) {}
            @Override public void onActivityPaused(Activity a) {}
            @Override public void onActivityStopped(Activity a) {}
            @Override public void onActivitySaveInstanceState(Activity a, Bundle b) {}
            @Override public void onActivityDestroyed(Activity a) {}
        });
    }

    private void attachGoogle(Activity activity) {
        if (!(activity instanceof MainActivity)) return;
        View welcome = findText(activity.getWindow().getDecorView(), "Welcome back 👋");
        if (!(welcome instanceof TextView)) return;
        if (!(welcome.getParent() instanceof LinearLayout)) return;
        LinearLayout parent = (LinearLayout) welcome.getParent();
        if (findGoogleButton(parent)) return;
        Button google = new Button(activity);
        google.setText("G  Continue with Google");
        google.setTextSize(16);
        google.setAllCaps(false);
        google.setTextColor(Color.DKGRAY);
        google.setMinHeight(dp(activity, 52));
        google.setBackground(round(Color.WHITE, dp(activity, 12)));
        google.setElevation(dp(activity, 2));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(activity, 52));
        lp.setMargins(0, dp(activity, 4), 0, dp(activity, 10));
        google.setLayoutParams(lp);
        google.setOnClickListener(v -> {
            google.setEnabled(false);
            NativeApi api = new NativeApi(activity);
            new GoogleAuth(activity).signIn(activity, api, new GoogleAuth.Callback() {
                @Override public void ok() {
                    activity.runOnUiThread(() -> {
                        Toast.makeText(activity, "Google से sign-in सफल", Toast.LENGTH_SHORT).show();
                        activity.recreate();
                    });
                }
                @Override public void error(String message) {
                    activity.runOnUiThread(() -> {
                        google.setEnabled(true);
                        Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
                    });
                }
            });
        });
        int index = parent.indexOfChild(welcome) + 1;
        parent.addView(google, index);
    }

    private boolean findGoogleButton(LinearLayout parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View v = parent.getChildAt(i);
            if (v instanceof Button && ((Button) v).getText().toString().contains("Google")) return true;
        }
        return false;
    }

    private View findText(View root, String wanted) {
        if (root instanceof TextView && wanted.contentEquals(((TextView) root).getText())) return root;
        if (root instanceof android.view.ViewGroup) {
            android.view.ViewGroup g = (android.view.ViewGroup) root;
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

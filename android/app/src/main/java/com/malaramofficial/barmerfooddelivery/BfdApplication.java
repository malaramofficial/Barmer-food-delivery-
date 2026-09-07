package com.malaramofficial.barmerfooddelivery;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/** Native app-wide typography and responsive UI polish. */
public final class BfdApplication extends Application {
    @Override public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override public void onActivityResumed(Activity activity) {
                polishTree(activity.getWindow().getDecorView());
            }
            @Override public void onActivityCreated(Activity a, Bundle b) {}
            @Override public void onActivityStarted(Activity a) {}
            @Override public void onActivityPaused(Activity a) {}
            @Override public void onActivityStopped(Activity a) {}
            @Override public void onActivitySaveInstanceState(Activity a, Bundle b) {}
            @Override public void onActivityDestroyed(Activity a) {}
        });
    }

    private void polishTree(View view) {
        if (view instanceof TextView) {
            TextView t = (TextView)view;
            if (!(t instanceof EditText)) {
                boolean bold = t.getTypeface() != null && t.getTypeface().isBold();
                t.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
                t.setIncludeFontPadding(true);
                t.setHorizontallyScrolling(false);
                if (t.getMaxLines() == Integer.MAX_VALUE) t.setMaxLines(4);
            } else {
                EditText e = (EditText)t;
                e.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
                e.setHorizontallyScrolling(false);
                e.setSingleLine(e.getMaxLines() == 1);
            }
        }
        if (view instanceof Button) {
            Button b = (Button)view;
            b.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            b.setAllCaps(false);
            b.setMinHeight(dp(view.getContext(), 48));
        }
        if (view instanceof ViewGroup) {
            ViewGroup g = (ViewGroup)view;
            for (int i = 0; i < g.getChildCount(); i++) polishTree(g.getChildAt(i));
        }
    }

    private int dp(Context c, int n) { return (int)(n * c.getResources().getDisplayMetrics().density + .5f); }
}

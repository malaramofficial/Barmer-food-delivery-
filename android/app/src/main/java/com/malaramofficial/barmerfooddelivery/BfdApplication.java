package com.malaramofficial.barmerfooddelivery;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/** Native app-wide typography polish and migration guard for the legacy customer auth view. */
public final class BfdApplication extends Application {
    @Override public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override public void onActivityResumed(Activity activity) {
                polishTree(activity.getWindow().getDecorView());
                if (activity instanceof MainActivity) watchLegacyLogin(activity);
            }
            @Override public void onActivityCreated(Activity a, Bundle b) {}
            @Override public void onActivityStarted(Activity a) {}
            @Override public void onActivityPaused(Activity a) {}
            @Override public void onActivityStopped(Activity a) {}
            @Override public void onActivitySaveInstanceState(Activity a, Bundle b) {}
            @Override public void onActivityDestroyed(Activity a) {}
        });
    }

    /** The old MainActivity login method still exists for compatibility. Redirect it to the new screen. */
    private void watchLegacyLogin(final Activity activity) {
        final View decor = activity.getWindow().getDecorView();
        final ViewTreeObserver observer = decor.getViewTreeObserver();
        final ViewTreeObserver.OnGlobalLayoutListener listener = new ViewTreeObserver.OnGlobalLayoutListener() {
            private boolean redirected;
            @Override public void onGlobalLayout() {
                if (redirected) return;
                View old = findText(decor, "Welcome back 👋");
                if (old != null) {
                    redirected = true;
                    if (observer.isAlive()) observer.removeOnGlobalLayoutListener(this);
                    activity.startActivity(new Intent(activity, LoginActivity.class));
                    activity.finish();
                }
            }
        };
        observer.addOnGlobalLayoutListener(listener);
        decor.postDelayed(() -> {
            if (observer.isAlive()) observer.removeOnGlobalLayoutListener(listener);
        }, 15000);
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

    private View findText(View root, String wanted) {
        if (root instanceof TextView && wanted.contentEquals(((TextView)root).getText())) return root;
        if (root instanceof ViewGroup) {
            ViewGroup g = (ViewGroup)root;
            for (int i = 0; i < g.getChildCount(); i++) {
                View found = findText(g.getChildAt(i), wanted);
                if (found != null) return found;
            }
        }
        return null;
    }

    private int dp(Context c, int n) { return (int)(n * c.getResources().getDisplayMetrics().density + .5f); }
}

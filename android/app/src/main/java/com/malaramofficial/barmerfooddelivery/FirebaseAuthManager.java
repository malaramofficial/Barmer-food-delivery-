package com.malaramofficial.barmerfooddelivery;

import android.content.Context;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/** Firebase Authentication bridge used by the native Android client. */
public final class FirebaseAuthManager {
    public interface Callback { void ok(String idToken); void error(String message); }

    private final FirebaseAuth auth;
    private final boolean configured;

    public FirebaseAuthManager(Context context) {
        Context app = context.getApplicationContext();
        boolean ready = false;
        try {
            FirebaseApp existing = null;
            try { existing = FirebaseApp.getInstance(); } catch (Exception ignored) { }
            if (existing == null) {
                String apiKey = safe(BuildConfig.FIREBASE_API_KEY);
                String appId = safe(BuildConfig.FIREBASE_APP_ID);
                String projectId = safe(BuildConfig.FIREBASE_PROJECT_ID);
                if (!apiKey.isEmpty() && !appId.isEmpty() && !projectId.isEmpty()) {
                    FirebaseOptions options = new FirebaseOptions.Builder()
                            .setApiKey(apiKey)
                            .setApplicationId(appId)
                            .setProjectId(projectId)
                            .build();
                    FirebaseApp.initializeApp(app, options);
                }
            }
            FirebaseApp.getInstance();
            ready = true;
        } catch (Exception ignored) {
            ready = false;
        }
        configured = ready;
        auth = ready ? FirebaseAuth.getInstance() : null;
    }

    public boolean configured() { return configured; }
    public FirebaseUser currentUser() { return auth == null ? null : auth.getCurrentUser(); }

    public void signInWithGoogleIdToken(String idToken, Callback callback) {
        if (!configured || auth == null) { callback.error("Firebase Auth अभी configure नहीं है।"); return; }
        if (idToken == null || idToken.trim().isEmpty()) { callback.error("Google ID token नहीं मिला।"); return; }
        auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
                .addOnCompleteListener(task -> finish(task, callback, "Firebase Google Sign-In failed."));
    }

    public void signInWithEmailPassword(String email, String password, Callback callback) {
        if (!configured || auth == null) { callback.error("Firebase Auth अभी configure नहीं है।"); return; }
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> finish(task, callback, "Firebase email login failed."));
    }

    public void createUserWithEmailPassword(String email, String password, Callback callback) {
        if (!configured || auth == null) { callback.error("Firebase Auth अभी configure नहीं है।"); return; }
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> finish(task, callback, "Firebase account creation failed."));
    }

    public void idToken(Callback callback) {
        if (!configured || auth == null) { callback.error("Firebase Auth अभी configure नहीं है।"); return; }
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) { callback.error("Firebase session उपलब्ध नहीं है।"); return; }
        user.getIdToken(false).addOnCompleteListener(task -> returnToken(task, callback, "Firebase ID token नहीं मिला।"));
    }

    public void forceRefreshToken(Callback callback) {
        if (!configured || auth == null || auth.getCurrentUser() == null) { callback.error("Firebase session उपलब्ध नहीं है।"); return; }
        auth.getCurrentUser().getIdToken(true).addOnCompleteListener(task -> returnToken(task, callback, "Firebase ID token refresh failed."));
    }

    public void signOut() { if (auth != null) auth.signOut(); }

    private static void finish(Task<AuthResult> task, Callback callback, String fallback) {
        if (!task.isSuccessful() || task.getResult() == null || task.getResult().getUser() == null) {
            Exception e = task.getException();
            callback.error(e == null || e.getMessage() == null ? fallback : e.getMessage());
            return;
        }
        task.getResult().getUser().getIdToken(true)
                .addOnCompleteListener(tokenTask -> returnToken(tokenTask, callback, "Firebase ID token नहीं मिला।"));
    }

    private static void returnToken(Task<com.google.firebase.auth.GetTokenResult> task, Callback callback, String fallback) {
        if (task.isSuccessful() && task.getResult() != null) {
            String token = task.getResult().getToken();
            if (token != null && !token.isEmpty()) { callback.ok(token); return; }
        }
        Exception e = task.getException();
        callback.error(e == null || e.getMessage() == null ? fallback : e.getMessage());
    }

    private static String safe(String value) { return value == null ? "" : value.trim(); }
}

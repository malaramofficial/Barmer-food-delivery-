package com.malaramofficial.barmerfooddelivery;

import android.app.Activity;
import android.content.Context;
import android.os.CancellationSignal;
import android.util.Base64;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.exceptions.GetCredentialException;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import java.security.SecureRandom;
import java.util.concurrent.Executor;

/** Native Google sign-in. Google credential -> Firebase Auth -> Firebase JWT -> Supabase. */
public final class GoogleAuth {
    public interface Callback { void ok(); void error(String message); }
    private final CredentialManager manager;
    private final Executor executor;
    private final FirebaseAuthManager firebase;

    public GoogleAuth(Context context) {
        this.manager = CredentialManager.create(context);
        this.executor = context.getMainExecutor();
        this.firebase = new FirebaseAuthManager(context);
    }

    private static String generateNonce() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.encodeToString(bytes, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
    }

    public void signIn(Activity activity, NativeApi api, Callback callback) {
        final String clientId = BuildConfig.GOOGLE_WEB_CLIENT_ID == null ? "" : BuildConfig.GOOGLE_WEB_CLIENT_ID.trim();
        if (clientId.isEmpty()) {
            callback.error("Google Sign-In अभी configure नहीं है. GOOGLE_WEB_CLIENT_ID जोड़ें.");
            return;
        }
        if (!firebase.configured()) {
            callback.error("Firebase Auth configure नहीं है। Firebase project की Android configuration जोड़ें।");
            return;
        }

        // This is the explicit button flow documented by Google for Sign in with Google.
        // It is intentionally used here because it also covers devices with no
        // previously-authorized Google account available to Credential Manager.
        GetSignInWithGoogleOption option = new GetSignInWithGoogleOption.Builder(clientId)
                .setNonce(generateNonce())
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(option)
                .build();

        manager.getCredentialAsync(activity, request, new CancellationSignal(), executor,
                new androidx.credentials.CredentialManagerCallback<androidx.credentials.GetCredentialResponse, GetCredentialException>() {
                    @Override public void onResult(androidx.credentials.GetCredentialResponse result) {
                        try {
                            Credential credential = result.getCredential();
                            if (!(credential instanceof androidx.credentials.CustomCredential)) {
                                callback.error("Google credential नहीं मिला"); return;
                            }
                            androidx.credentials.CustomCredential custom = (androidx.credentials.CustomCredential) credential;
                            if (!GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(custom.getType())) {
                                callback.error("Unsupported Google credential"); return;
                            }
                            GoogleIdTokenCredential google = GoogleIdTokenCredential.createFrom(custom.getData());
                            firebase.signInWithGoogleIdToken(google.getIdToken(), new FirebaseAuthManager.Callback() {
                                @Override public void ok(String firebaseToken) {
                                    String uid = firebase.currentUser() == null ? "" : firebase.currentUser().getUid();
                                    api.setFirebaseSession(firebaseToken, uid);
                                    callback.ok();
                                }
                                @Override public void error(String message) { callback.error(message); }
                            });
                        } catch (Exception e) {
                            callback.error(e.getMessage() == null ? "Google Sign-In failed" : e.getMessage());
                        }
                    }

                    @Override public void onError(GetCredentialException e) {
                        String message = e.getMessage();
                        if (e instanceof androidx.credentials.exceptions.NoCredentialException) {
                            callback.error("Google account उपलब्ध नहीं है। फोन में Google account जोड़कर फिर से Continue with Google दबाएँ।");
                        } else {
                            callback.error(message == null ? "Google Sign-In cancelled or failed" : message);
                        }
                    }
                });
    }
}

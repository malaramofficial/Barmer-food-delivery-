package com.malaramofficial.barmerfooddelivery;

import android.app.Activity;
import android.content.Context;
import android.os.CancellationSignal;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.exceptions.GetCredentialException;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import org.json.JSONObject;
import java.util.concurrent.Executor;

/** Native Google Identity sign-in. The ID token is exchanged with Supabase Auth. */
public final class GoogleAuth {
    public interface Callback { void ok(); void error(String message); }
    private final Context context;
    private final CredentialManager manager;
    private final Executor executor;

    public GoogleAuth(Context context) {
        this.context = context;
        this.manager = CredentialManager.create(context);
        this.executor = context.getMainExecutor();
    }

    public void signIn(Activity activity, NativeApi api, Callback callback) {
        final String clientId = BuildConfig.GOOGLE_WEB_CLIENT_ID == null ? "" : BuildConfig.GOOGLE_WEB_CLIENT_ID.trim();
        if (clientId.isEmpty()) {
            callback.error("Google Sign-In अभी configure नहीं है. GOOGLE_WEB_CLIENT_ID जोड़ें.");
            return;
        }
        GetGoogleIdOption option = new GetGoogleIdOption.Builder()
                .setServerClientId(clientId)
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(true)
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
                            JSONObject body = new JSONObject();
                            body.put("provider", "google");
                            body.put("id_token", google.getIdToken());
                            api.signInWithIdToken(body, callback);
                        } catch (Exception e) { callback.error(e.getMessage() == null ? "Google Sign-In failed" : e.getMessage()); }
                    }
                    @Override public void onError(GetCredentialException e) {
                        callback.error(e.getMessage() == null ? "Google Sign-In cancelled or failed" : e.getMessage());
                    }
                });
    }
}

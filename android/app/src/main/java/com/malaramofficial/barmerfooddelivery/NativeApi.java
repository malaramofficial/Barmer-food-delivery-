package com.malaramofficial.barmerfooddelivery;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.firebase.auth.FirebaseUser;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Native Supabase data client authenticated only with Firebase JWTs. */
public final class NativeApi {
    public interface Callback { void ok(JSONObject data); void error(String message); }
    private static final String PREF = "bfd_session";
    private final SharedPreferences prefs;
    private final ExecutorService io = Executors.newCachedThreadPool();
    private final String base, anon;
    private final FirebaseAuthManager firebase;

    public NativeApi(Context c) {
        Context app = c.getApplicationContext();
        prefs = app.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        base = BuildConfig.SUPABASE_URL == null ? "" : BuildConfig.SUPABASE_URL.trim().replaceAll("/$", "");
        anon = BuildConfig.SUPABASE_ANON_KEY == null ? "" : BuildConfig.SUPABASE_ANON_KEY.trim();
        firebase = new FirebaseAuthManager(app);
    }

    public boolean configured() { return !base.isEmpty() && !anon.isEmpty(); }
    public boolean firebaseSession() { return prefs.getBoolean("firebase_session", false); }
    public String accessToken() { return prefs.getString("access_token", ""); }
    /** Kept only for source compatibility; Firebase owns token refresh now. */
    public String refreshToken() { return ""; }
    public String userId() { return prefs.getString("user_id", ""); }
    public boolean signedIn() { return firebaseSession() && firebase.currentUser() != null; }

    public String configurationStatus() {
        if (!base.isEmpty() && !anon.isEmpty() && firebase.configured()) return "Firebase Auth + Supabase data backend ready";
        if (!firebase.configured()) return "Firebase Auth configure नहीं है।";
        if (base.isEmpty() && anon.isEmpty()) return "Supabase data URL और client key configure नहीं हैं।";
        if (base.isEmpty()) return "Supabase data URL configure नहीं है।";
        if (anon.isEmpty()) return "Supabase client key configure नहीं है।";
        return "Backend configuration incomplete";
    }

    public void setFirebaseSession(String token, String uid) {
        prefs.edit().putBoolean("firebase_session", true)
                .putString("access_token", token == null ? "" : token)
                .putString("user_id", uid == null ? "" : uid)
                .apply();
    }

    public void signOut() {
        prefs.edit().clear().apply();
        firebase.signOut();
    }

    /** Create/find the app profile after Firebase authentication. Supabase Auth is not used. */
    public void ensureFirebaseProfile(String fullName, String phone, Callback cb) {
        FirebaseUser user = firebase.currentUser();
        if (user == null || !firebaseSession()) { cb.error("Firebase session उपलब्ध नहीं है।"); return; }
        JSONObject body = new JSONObject();
        try {
            body.put("p_full_name", fullName == null ? "Customer" : fullName.trim());
            body.put("p_phone", phone == null ? "" : phone.trim());
        } catch (Exception e) { cb.error(e.getMessage()); return; }
        request("POST", "/rest/v1/rpc/ensure_firebase_profile", body, true, new Callback() {
            @Override public void ok(JSONObject d) {
                try {
                    JSONArray a = d.optJSONArray("data");
                    JSONObject row = a == null ? null : a.optJSONObject(0);
                    String id = row == null ? "" : row.optString("profile_id", "");
                    if (id.isEmpty()) { cb.error("Firebase profile ID नहीं मिला।"); return; }
                    prefs.edit().putString("user_id", id).apply();
                    cb.ok(d);
                } catch (Exception e) { cb.error("Firebase profile तैयार नहीं हो सका।"); }
            }
            @Override public void error(String m) { cb.error(m); }
        }, true);
    }

    public void restaurants(Callback cb) {
        request("GET", "/rest/v1/restaurants?select=id,name,cuisine,address,area,rating,delivery_fee,is_open,latitude,longitude&is_approved=eq.true&order=name", null, false, cb, false);
    }

    public void menu(String id, Callback cb) {
        if (id == null || id.trim().isEmpty()) { cb.error("Restaurant id missing"); return; }
        request("GET", "/rest/v1/menu_items?select=id,name,description,price,image_url,is_available&restaurant_id=eq." + id + "&is_available=eq.true&order=name", null, false, cb, false);
    }

    public void orders(Callback cb) {
        String uid = userId();
        if (uid.isEmpty()) { cb.error("Please login first"); return; }
        request("GET", "/rest/v1/orders?select=id,restaurant_id,status,delivery_address,delivery_latitude,delivery_longitude,subtotal,delivery_fee,total,payment_method,created_at&customer_id=eq." + uid + "&order=created_at.desc&limit=30", null, true, cb, true);
    }

    public void notifications(Callback cb) {
        String uid = userId();
        if (uid.isEmpty()) { cb.error("Please login first"); return; }
        request("GET", "/rest/v1/notifications?select=id,title,body,order_id,read_at,created_at&user_id=eq." + uid + "&order=created_at.desc&limit=30", null, true, cb, true);
    }

    public void profile(Callback cb) {
        String uid = userId();
        if (uid.isEmpty()) { cb.error("Please login first"); return; }
        request("GET", "/rest/v1/profiles?select=id,full_name,phone,role,address&id=eq." + uid + "&limit=1", null, true, cb, true);
    }

    public void adminDashboard(Callback cb) { invoke("admin-dashboard", new JSONObject(), cb); }

    public void createOrder(String restaurantId, String address, double lat, double lng, String payment, JSONArray items, Callback cb) {
        JSONObject b = new JSONObject();
        try {
            b.put("restaurant_id", restaurantId);
            b.put("address", address);
            b.put("latitude", lat);
            b.put("longitude", lng);
            b.put("payment_method", payment);
            b.put("items", items);
        } catch (Exception e) { cb.error(e.getMessage()); return; }
        invoke("create-order", b, cb);
    }

    public void applyRestaurant(JSONObject b, Callback cb) { insert("restaurant_applications", b, cb); }
    public void applyRider(JSONObject b, Callback cb) { insert("rider_applications", b, cb); }
    public void invoke(String fn, JSONObject body, Callback cb) { request("POST", "/functions/v1/" + fn, body, true, cb, true); }
    private void insert(String table, JSONObject body, Callback cb) { request("POST", "/rest/v1/" + table, body, true, cb, true); }

    private void request(String method, String path, JSONObject body, boolean auth, Callback cb, boolean retryOn401) {
        io.execute(() -> {
            if (!configured()) { cb.error("BACKEND_NOT_CONFIGURED: " + configurationStatus()); return; }
            if (auth && (!firebaseSession() || firebase.currentUser() == null)) { cb.error("Please login with Firebase first"); return; }
            if (auth) {
                firebase.idToken(new FirebaseAuthManager.Callback() {
                    @Override public void ok(String token) {
                        prefs.edit().putString("access_token", token).apply();
                        requestOnce(method, path, body, true, retryOn401, cb);
                    }
                    @Override public void error(String m) { cb.error(m); }
                });
            } else requestOnce(method, path, body, false, retryOn401, cb);
        });
    }

    private void refreshFirebaseAndRetry(String method, String path, JSONObject body, Callback cb) {
        firebase.forceRefreshToken(new FirebaseAuthManager.Callback() {
            @Override public void ok(String token) {
                prefs.edit().putString("access_token", token).apply();
                requestOnce(method, path, body, true, false, cb);
            }
            @Override public void error(String m) { signOut(); cb.error("Firebase session expired. Please login again."); }
        });
    }

    private void requestOnce(String method, String path, JSONObject body, boolean auth, boolean retryOn401, Callback cb) {
        HttpURLConnection c = null;
        try {
            c = (HttpURLConnection) new URL(base + path).openConnection();
            c.setRequestMethod(method);
            c.setConnectTimeout(15000);
            c.setReadTimeout(20000);
            c.setRequestProperty("apikey", anon);
            c.setRequestProperty("Accept", "application/json");
            if (auth) c.setRequestProperty("Authorization", "Bearer " + accessToken());
            if (body != null) {
                c.setRequestProperty("Content-Type", "application/json");
                c.setRequestProperty("Prefer", "return=representation");
                c.setDoOutput(true);
                try (OutputStream o = c.getOutputStream()) { o.write(body.toString().getBytes(StandardCharsets.UTF_8)); }
            }
            int code = c.getResponseCode();
            String s = read(code >= 200 && code < 300 ? c.getInputStream() : c.getErrorStream());
            if (code == 401 && auth && retryOn401) { refreshFirebaseAndRetry(method, path, body, cb); return; }
            JSONObject out;
            try {
                if (s.trim().startsWith("[")) out = new JSONObject().put("data", new JSONArray(s));
                else if (s.trim().isEmpty()) out = new JSONObject();
                else out = new JSONObject(s);
            } catch (Exception e) { out = new JSONObject().put("raw", s); }
            if (code >= 200 && code < 300) cb.ok(out); else cb.error(errorMessage(code, out, s));
        } catch (Exception e) {
            cb.error(e.getMessage() == null ? "Network error" : e.getMessage());
        } finally { if (c != null) c.disconnect(); }
    }

    private String errorMessage(int code, JSONObject out, String raw) {
        String m = out.optString("message", out.optString("error_description", out.optString("error", "")));
        if (m.isEmpty()) m = raw;
        if (m == null || m.isEmpty()) m = "Request failed (HTTP " + code + ")";
        return m.length() > 300 ? m.substring(0, 300) : m;
    }

    private String read(InputStream in) throws IOException {
        if (in == null) return "";
        StringBuilder b = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String l;
            while ((l = r.readLine()) != null) b.append(l);
        }
        return b.toString();
    }
}

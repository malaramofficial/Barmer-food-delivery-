package com.malaramofficial.barmerfooddelivery;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Native Android entry point. No WebView and no browser-hosted UI.
 * The feature screens are native Android Views and are ready to be connected
 * to the existing Supabase backend through NativeApi.
 */
public class MainActivity extends Activity {
    private static final int LOCATION_REQUEST = 1001;
    private LinearLayout root;
    private final List<String> cart = new ArrayList<>();

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showHome();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView text(String value, float size, int color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setPadding(dp(16), dp(10), dp(16), dp(10));
        return t;
    }

    private Button button(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setMinHeight(dp(48));
        return b;
    }

    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(12), dp(12), dp(12), dp(16));
        return l;
    }

    private void base(String title) {
        root = column();
        root.setBackgroundColor(Color.WHITE);
        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        TextView t = text(title, 22, Color.rgb(30, 30, 30));
        t.setTypeface(null, android.graphics.Typeface.BOLD);
        bar.addView(t, new LinearLayout.LayoutParams(0, dp(64), 1));
        Button profile = button("Profile");
        profile.setOnClickListener(v -> showProfile());
        bar.addView(profile, new LinearLayout.LayoutParams(dp(100), dp(56)));
        root.addView(bar);
        setContentView(root);
    }

    private void showHome() {
        base("Barmer Food Delivery");
        TextView welcome = text("बारमेर में खाना अब आपके दरवाज़े तक", 18, Color.DKGRAY);
        root.addView(welcome);

        Button location = button("📍 Delivery location सेट करें");
        location.setOnClickListener(v -> requestLocation());
        root.addView(location);

        root.addView(text("Categories", 18, Color.BLACK));
        HorizontalScrollView hsv = new HorizontalScrollView(this);
        LinearLayout cats = new LinearLayout(this);
        cats.setOrientation(LinearLayout.HORIZONTAL);
        String[] categories = {"🍛 All", "🍕 Pizza", "🍔 Burger", "🥘 Rajasthani", "🍗 Biryani", "☕ Cafe"};
        for (String c : categories) {
            Button b = button(c);
            b.setOnClickListener(v -> showRestaurants(c));
            cats.addView(b, new LinearLayout.LayoutParams(dp(125), dp(56)));
        }
        hsv.addView(cats);
        root.addView(hsv);

        root.addView(text("Approved restaurants & hotels", 18, Color.BLACK));
        addRestaurantCard("Barmer Food Corner", "North Indian • 25–35 min", "₹₹", "🍛");
        addRestaurantCard("Marwar Rasoi", "Rajasthani • 30–40 min", "₹₹", "🥘");
        addRestaurantCard("Desert Cafe", "Cafe & Snacks • 15–25 min", "₹", "☕");

        Button orders = button("📦 My Orders");
        orders.setOnClickListener(v -> showOrders());
        root.addView(orders);

        Button partner = button("Careers / Partner with us");
        partner.setOnClickListener(v -> showPartner());
        root.addView(partner);
    }

    private void addRestaurantCard(String name, String details, String price, String emoji) {
        LinearLayout card = column();
        card.setBackgroundColor(Color.rgb(248, 248, 248));
        TextView title = text(emoji + "  " + name, 19, Color.BLACK);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(title);
        card.addView(text(details + "   " + price, 14, Color.DKGRAY));
        Button menu = button("View Menu");
        menu.setOnClickListener(v -> showMenu(name));
        card.addView(menu);
        root.addView(card, new LinearLayout.LayoutParams(-1, LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    private void showRestaurants(String category) {
        base(category + " Restaurants");
        addRestaurantCard("Barmer Food Corner", "Approved • Fast delivery", "₹₹", "🍛");
        addRestaurantCard("Marwar Rasoi", "Approved • Rajasthani", "₹₹", "🥘");
        addRestaurantCard("Desert Cafe", "Approved • Snacks", "₹", "☕");
        Button back = button("← Home");
        back.setOnClickListener(v -> showHome());
        root.addView(back);
    }

    private void showMenu(String restaurant) {
        base(restaurant);
        root.addView(text("Menu", 20, Color.BLACK));
        addFood("Dal Baati Churma", "₹180");
        addFood("Paneer Thali", "₹220");
        addFood("Masala Dosa", "₹120");
        addFood("Cold Drink", "₹50");
        Button cartButton = button("🛒 Cart (" + cart.size() + ")");
        cartButton.setOnClickListener(v -> showCart());
        root.addView(cartButton);
    }

    private void addFood(String name, String price) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView t = text(name + "\n" + price, 16, Color.DKGRAY);
        row.addView(t, new LinearLayout.LayoutParams(0, dp(70), 1));
        Button add = button("+ Add");
        add.setOnClickListener(v -> {
            cart.add(name);
            Toast.makeText(this, name + " cart में जोड़ा गया", Toast.LENGTH_SHORT).show();
        });
        row.addView(add, new LinearLayout.LayoutParams(dp(95), dp(55)));
        root.addView(row);
    }

    private void showCart() {
        base("Your Cart");
        if (cart.isEmpty()) root.addView(text("Cart खाली है", 18, Color.GRAY));
        else {
            for (String item : cart) root.addView(text("• " + item, 16, Color.DKGRAY));
            Button checkout = button("Proceed to Checkout • COD");
            checkout.setOnClickListener(v -> showCheckout());
            root.addView(checkout);
        }
        Button home = button("← Continue Shopping");
        home.setOnClickListener(v -> showHome());
        root.addView(home);
    }

    private void showCheckout() {
        base("Checkout");
        root.addView(text("Delivery address", 18, Color.BLACK));
        root.addView(text("आपका चुना हुआ delivery location यहाँ दिखेगा", 15, Color.DKGRAY));
        Button loc = button("📍 Use current location");
        loc.setOnClickListener(v -> requestLocation());
        root.addView(loc);
        root.addView(text("Payment: Cash on Delivery", 16, Color.DKGRAY));
        Button place = button("Place Order");
        place.setOnClickListener(v -> {
            cart.clear();
            Toast.makeText(this, "Order placed — backend connection required for live dispatch", Toast.LENGTH_LONG).show();
            showOrders();
        });
        root.addView(place);
    }

    private void showOrders() {
        base("My Orders");
        root.addView(text("No active orders", 18, Color.GRAY));
        Button tracking = button("Open Live Tracking");
        tracking.setOnClickListener(v -> showTracking());
        root.addView(tracking);
        Button home = button("← Home");
        home.setOnClickListener(v -> showHome());
        root.addView(home);
    }

    private void showTracking() {
        base("Live Order Tracking");
        root.addView(text("🟢 Restaurant → Rider → You", 19, Color.BLACK));
        root.addView(text("Native map/tracking service will use GPS + backend realtime data.", 15, Color.DKGRAY));
        Button maps = button("Open device Maps");
        maps.setOnClickListener(v -> {
            try { startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)); }
            catch (Exception ignored) { }
        });
        root.addView(maps);
    }

    private void showPartner() {
        base("Partner with Barmer Food Delivery");
        root.addView(text("Choose your role", 20, Color.BLACK));
        Button restaurant = button("🏨 Restaurant / Hotel Partner");
        restaurant.setOnClickListener(v -> showApplication("Restaurant / Hotel Partner"));
        root.addView(restaurant);
        Button rider = button("🛵 Delivery Rider");
        rider.setOnClickListener(v -> showApplication("Delivery Rider"));
        root.addView(rider);
        root.addView(text("Approval, KYC और service-area checks admin द्वारा होंगे.", 14, Color.GRAY));
    }

    private void showApplication(String role) {
        base(role);
        root.addView(text("Native application form", 20, Color.BLACK));
        root.addView(text("नाम, मोबाइल, पता, KYC/vehicle details और आवश्यक documents backend में सुरक्षित रूप से submit होंगे.", 15, Color.DKGRAY));
        Button submit = button("Submit Application");
        submit.setOnClickListener(v -> Toast.makeText(this, "Login/backend integration required", Toast.LENGTH_LONG).show());
        root.addView(submit);
    }

    private void showProfile() {
        base("Profile");
        root.addView(text("Customer Account", 20, Color.BLACK));
        Button login = button("Login / Sign up");
        login.setOnClickListener(v -> Toast.makeText(this, "Native authentication screen", Toast.LENGTH_SHORT).show());
        root.addView(login);
        Button partner = button("Careers / Partner with us");
        partner.setOnClickListener(v -> showPartner());
        root.addView(partner);
    }

    private void requestLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST);
        } else {
            Toast.makeText(this, "GPS permission उपलब्ध है", Toast.LENGTH_SHORT).show();
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == LOCATION_REQUEST) {
            Toast.makeText(this, results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED ? "Location enabled" : "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}

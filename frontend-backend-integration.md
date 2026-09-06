# Frontend ↔ Supabase integration checkpoint

The browser-side Supabase bridge is now available in `supabase-client.js`.

It provides safe helpers for:
- authenticated session/user lookup
- approved restaurant listing
- restaurant menu loading
- create-order Edge Function
- order-action Edge Function
- tracking Edge Function
- order notification Edge Function
- realtime order updates
- realtime user notifications

No service-role credential is included. `config.js` remains empty by default, so the app cannot accidentally claim production connectivity until public Supabase URL + anon key are configured.

Next integration step: wire the existing UI in `app.js` to these helpers without replacing the current demo UI until the complete source can be safely patched.

# Barmer Food Delivery — Release Gate

## Implemented in this repository
- Mobile-first customer ordering prototype
- Restaurant and rider onboarding flows
- Production-oriented Supabase schema with RLS
- Secure server-side order creation function
- Server-side admin review function
- Nearest-rider dispatch function using fresh GPS heartbeat
- PWA manifest, service worker and app icon
- Privacy Policy and Terms pages
- Environment/secrets template
- Barmer restaurant seed data

## Required before real public launch
1. Create the production Supabase project and run `supabase/schema.sql` and `supabase/seed.sql`.
2. Configure Supabase Auth, private KYC storage, Realtime and Edge Functions.
3. Deploy Edge Functions and set `SUPABASE_SERVICE_ROLE_KEY` only as a server-side secret.
4. Configure the production browser Supabase URL/anon key through the hosting environment; never commit secrets.
5. Complete RLS review for every table and add policies for restaurant/rider operational actions.
6. Connect the customer UI to Supabase Auth, realtime orders/notifications and the Edge Functions.
7. Configure real map/geocoding, Barmer service-area validation and rider GPS tracking.
8. Configure a production payment provider and webhook/refund flow if online payments are enabled.
9. Configure SMS/push/WhatsApp notification provider as required.
10. Add real approved restaurants, menus, pricing, delivery zones, support contact and business/legal details.
11. Replace demo/admin UI with authenticated role-based dashboards and server-side authorization.
12. Test cancellation, refunds, duplicate orders, network loss, location permission denial, stale GPS, concurrent rider acceptance and fraud/abuse cases.
13. Publish the web/PWA build over HTTPS and verify installability and offline behavior.
14. For Google Play: package the production web app as a compliant Android app/AAB, configure application ID, signing key, privacy/data-safety declarations, screenshots, icon, content rating, target SDK and Play App Signing, then complete Play Console testing/review.

## Important release status
This repository is **not yet honestly claimable as a production-ready or Play-Store-ready food delivery service** until the production backend credentials, real payment/maps/notifications, authenticated dashboards, operational policies, testing, Android AAB/signing and Play Console declarations are completed and verified. The demo UI must not be presented to customers as a live service.

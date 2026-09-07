# Barmer Food Delivery — Code Audit

Date: 2026-09-07
Scope: native Android app, Supabase schema/migrations, Edge Function order/auth/security paths, build pipeline.

## Critical findings found and remediated

1. **Android backend configuration was silently empty**
   - `SUPABASE_URL` and `SUPABASE_ANON_KEY` default to empty build values.
   - Native client now exposes a precise configuration status and returns `BACKEND_NOT_CONFIGURED` instead of a vague runtime failure.
   - Production build still requires the project's GitHub Actions secrets to be populated; source code does not hard-code credentials.

2. **Auth sessions had no refresh flow**
   - Added refresh-token exchange on HTTP 401 and secure session replacement.
   - Authenticated requests retry once after a successful refresh; failed refresh clears the session.

3. **Profile lookup was not explicitly user-scoped**
   - Native API now queries `profiles` by the authenticated user's stored UUID.

4. **New Auth users were not guaranteed a `profiles` row**
   - Added `on_auth_user_created` trigger and `handle_new_user()` security-definer function.

5. **Client could attempt to change its own role**
   - Existing profile update policy was too broad for a role-bearing table.
   - Added a database trigger that preserves `role`, `id`, and `created_at` for client-originated updates. Server-side/service-role operations can still change roles.

6. **Customers could directly insert orders**
   - Removed the direct `orders` INSERT policy.
   - Orders must now pass through the server-side `create-order` function, which recalculates menu prices and delivery fee.

7. **Review creation was under-validated**
   - Reviews now require the submitting customer to own the order, match the restaurant, and have a delivered order.

8. **Notification update surface was too broad**
   - Added a trigger protecting notification ownership/content so clients can effectively change read state without rewriting notification data.

9. **Admin entry existed but did not provide an operations destination**
   - Added a native Admin & Staff login and a native operations overview dashboard backed by the server-side `admin-dashboard` function.
   - Admin role remains backend-authoritative.

10. **UI typography/layout was vulnerable to OEM fonts and small windows**
    - Added an adaptive typography pass using explicit Android sans-serif fonts, wrapping, minimum touch heights and non-clipping text behavior.
    - Enabled `adjustResize` for customer/admin activities.

## UI direction

The customer experience is being shaped around fast browse → search/location → restaurant → menu → cart → checkout → tracking, with clear primary actions, large touch targets, restrained cards, and responsive spacing. Android's current adaptive guidance recommends responsive layouts based on available window size rather than device-specific fixed layouts. Material 3 recommends navigation components that change with compact/medium/expanded window sizes.

Reference principles:
- Android adaptive display guidance
- Material 3 navigation and responsive layouts
- Native food-delivery patterns: location-first browsing, fast checkout, live tracking and clear order states

## Remaining production gates

These cannot be safely fabricated in source code:

- Real Supabase project URL + publishable/anon key must be supplied to the Android release build.
- Supabase migrations in this repository must actually be applied to the production project.
- Google provider/client configuration must match the production Supabase Auth configuration.
- Maps, payment provider, push notification and SMS credentials must be configured externally.
- Production signing credentials and Play Console configuration are external release secrets.

No service-role key or signing secret is committed to the repository.

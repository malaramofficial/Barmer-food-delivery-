# Barmer Food Delivery — Production Release Gate

## What the repository can do
The repository contains the customer PWA flow, partner onboarding, rider dispatch/tracking foundations, private KYC upload/review flow, operational/admin foundations, security migrations, and release documentation.

## Final external release steps
These cannot be safely fabricated by source-code changes:

1. Create/verify the production Supabase project.
2. Apply all migrations in order and verify RLS policies.
3. Deploy all Supabase Edge Functions.
4. Set `SUPABASE_URL`, `SUPABASE_ANON_KEY`, and `SUPABASE_SERVICE_ROLE_KEY` only in Supabase server-side configuration.
5. Create/verify the private `kyc-documents` Storage bucket and its storage policies.
6. Configure production map/geocoding credentials if a paid provider is selected.
7. Configure a real payment provider and provider-specific signed webhook verification before enabling online payments.
8. Configure production push/SMS/WhatsApp notifications if required.
9. Verify the Barmer service area and business/legal operating policy.
10. Create the real admin account through the approved authentication process; never hard-code admin credentials.
11. Load only verified restaurants, menus, prices, delivery fees and operating hours.
12. Test customer, restaurant, rider and admin roles with separate accounts.
13. Run KYC upload/review tests with non-real test documents.
14. Run order, rider acceptance, GPS, cancellation, payment and notification tests under network loss and stale-location conditions.
15. Publish only over HTTPS.
16. For Android/Google Play, create a proper native/web-wrapper project, application ID, signing key/AAB, privacy policy, data-safety declarations, screenshots, content rating, target SDK and Play Console release.

## Safety rule
No production secret, signing key, payment credential, real KYC document, or legal declaration belongs in this public repository.

## Release status
**Code complete for the repository scope; external production release gate remains.** A GitHub repository alone cannot create or verify third-party accounts, secrets, signing keys, legal approvals, or Play Console review.

# Barmer Food Delivery — Release Gate

## Repository engineering status
The planned repository engineering batch is complete for the implemented scope: customer ordering/PWA foundations, partner onboarding, rider dispatch/tracking, private KYC upload/review, operational/admin foundations, security migrations, validation helpers and release documentation.

## External production gates (must be completed outside public source control)
1. Production Supabase project created and migrations/schema/seed applied.
2. Supabase Auth, Realtime, private `kyc-documents` Storage and Edge Functions configured/deployed.
3. Server-only secrets configured: `SUPABASE_SERVICE_ROLE_KEY` and other provider secrets.
4. Browser Supabase URL/anon key configured without committing secrets.
5. Full production RLS review and role/authorization verification completed.
6. Real map/geocoding and exact Barmer service-area policy configured and tested.
7. Production payment provider, provider-specific signed webhook verification and refund process configured before online payments are enabled.
8. Production push/SMS/WhatsApp notification provider configured if required.
9. Real restaurant/rider onboarding, operating policies, menus, pricing, fees and support contacts loaded.
10. Full cancellation, refund, duplicate-order, network-loss, stale-GPS, location-denial, concurrent-acceptance and abuse testing completed.
11. HTTPS deployment and PWA install/offline verification completed.
12. Android wrapper/native build, application ID, signing key, AAB, target SDK, privacy/data-safety declarations, screenshots, content rating and Play Console testing/review completed before Play release.
13. KYC legal/privacy review completed: consent, retention, deletion, access control and applicable identity-verification requirements.

## Important truth-in-release rule
The public GitHub repository must never contain production secrets, service-role keys, payment credentials, signing keys, real KYC documents, or private customer data. External credentials, legal approvals, account creation and Play Console review cannot be fabricated by source-code changes.

## Final status
**Repository engineering complete; external production-release gates remain.** Do not market the current GitHub demo/PWA as a live production food-delivery service until the gates above are independently verified.

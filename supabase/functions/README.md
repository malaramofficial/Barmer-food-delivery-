# Barmer Food Delivery Edge Functions

Production functions require Supabase environment variables `SUPABASE_URL`, `SUPABASE_ANON_KEY`, and `SUPABASE_SERVICE_ROLE_KEY`.

`BFD_BARMER_RADIUS_KM` optionally controls the Barmer service radius and defaults to 35 km.

Never expose the service-role key in frontend code. Deploy and configure these functions in the production Supabase project before public release.

## KYC functions

- `kyc-submit` — authenticated upload metadata submission; verifies the private Storage object, MIME type and size.
- `kyc-signed-url` — authenticated owner/admin access through a 5-minute signed URL.
- `kyc-review` — admin-only approve/reject action and applicant notification.

Required private Storage bucket: `kyc-documents`.
Allowed files: JPEG, PNG, PDF. Maximum: 5 MB.

## Recommended deployment order

1. Apply `supabase/migrations/20260907_kyc_storage_flow.sql`.
2. Apply `supabase/migrations/20260907_audit_logs.sql`.
3. Deploy `kyc-submit`, `kyc-signed-url`, and `kyc-review`.
4. Configure secrets only in Supabase Edge Functions.
5. Confirm bucket visibility is private.
6. Run `supabase/tests/kyc-security-smoke.sql` with synthetic test accounts.

## KYC privacy

Do not log raw documents, document contents, Aadhaar/PAN numbers, signed URLs, or service-role credentials. Signed URLs are intentionally short-lived. Legal retention/deletion and consent requirements must be reviewed before launch.

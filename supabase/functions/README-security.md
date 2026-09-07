# Security / production configuration

Before production, configure these Supabase secrets (never commit them):

- `SUPABASE_URL`
- `SUPABASE_ANON_KEY`
- `SUPABASE_SERVICE_ROLE_KEY`
- `BFD_BARMER_RADIUS_KM` (default 35)
- `BFD_PAYMENT_WEBHOOK_SECRET`

Use a private Storage bucket for KYC documents. Storage object paths must begin with the authenticated user's UUID. Generate short-lived signed URLs for admin review; never expose the bucket publicly.

Payment webhooks must be served over HTTPS and verified with a provider signature/secret. The included webhook header secret is a deployment baseline, not a substitute for provider-specific signature verification.

Run all migrations in `supabase/migrations/` before enabling these functions.

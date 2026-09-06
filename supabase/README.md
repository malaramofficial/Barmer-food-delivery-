# Backend setup

1. Create a Supabase project.
2. Run `schema.sql` in the SQL editor.
3. Enable Email/Phone authentication as required.
4. Create private storage buckets for KYC/verification documents; do not expose them publicly.
5. Add the project URL and anon key to the application environment.
6. Use Edge Functions/server-side code for admin review, restaurant notifications, rider dispatch, payment webhooks and any privileged operation.
7. Enable Realtime only for tables/events required by the active order flow.

Never commit service-role keys, payment secrets or admin passwords to this repository.

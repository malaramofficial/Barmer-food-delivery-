# Barmer Food Delivery Edge Functions

Production functions require Supabase environment variables `SUPABASE_URL`, `SUPABASE_ANON_KEY`, and `SUPABASE_SERVICE_ROLE_KEY`.

`BFD_BARMER_RADIUS_KM` optionally controls the Barmer service radius and defaults to 35 km.

Never expose the service-role key in frontend code. Deploy and configure these functions in the production Supabase project before public release.

# Barmer Food Delivery — KYC production flow

## Implemented flow
1. User signs in with Supabase Auth.
2. Partner Centre exposes a dedicated KYC portal.
3. User selects application type and document type.
4. Browser validates MIME and 5 MB size before upload.
5. Browser generates a UUID-based object name under `<user-id>/`.
6. File is uploaded to the **private** `kyc-documents` Storage bucket.
7. `kyc-submit` authenticates the user and verifies the object exists.
8. KYC metadata is stored as `pending` without exposing a public URL.
9. User sees document status and rejection notes.
10. Admin sees a pending KYC review queue.
11. Admin opens the document through a short-lived signed URL.
12. Admin approves/rejects through `kyc-review` server-side authorization.
13. The applicant receives a notification about the review result.
14. Review metadata is written to the operational audit trail.

## Security rules
- Never make `kyc-documents` public.
- Never place a Supabase service-role key in browser code.
- Never store raw KYC files in GitHub.
- Never put Aadhaar/PAN numbers into audit logs, notifications, analytics, or URLs.
- Signed URLs expire after 5 minutes.
- Object paths must begin with the authenticated user's UUID.
- Allowed MIME types: JPEG, PNG, PDF.
- Maximum file size: 5 MB.
- Admin review is server-authorized.

## Deployment requirements
Apply migrations in order, deploy the three KYC Edge Functions, and confirm Storage policies in the Supabase dashboard. Configure production Supabase URL/anon key in the frontend only; the service-role key remains an Edge Function secret.

## Important production limitation
This implements the actual private upload/review pipeline, but legal KYC retention, deletion, consent wording, identity-verification vendor integration, and Play Store privacy disclosures still need a final compliance review before public launch.

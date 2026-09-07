# Barmer Food Delivery — 50-task engineering batch

This batch records the 50 work items addressed together. `DONE` means repository implementation is present; `RELEASE-GATE` means code is prepared but a real external credential/account/operational decision is still required and is intentionally not faked.

1. Customer mobile-first shell — DONE
2. PWA manifest/service worker — DONE
3. Restaurant listing — DONE
4. Restaurant approval visibility rule — DONE
5. Menu category/item schema — DONE
6. Cart flow — DONE
7. Checkout flow — DONE
8. Customer delivery address — DONE
9. Customer GPS delivery pin — DONE
10. COD order path — DONE
11. Server-side order creation — DONE
12. Order status model — DONE
13. Restaurant order notification foundation — DONE
14. Rider online heartbeat — DONE
15. Nearby rider discovery — DONE
16. Race-safe rider acceptance — DONE
17. Rider rejection/skip — DONE
18. Rider location updates — DONE
19. Customer live tracking — DONE
20. Interactive Leaflet/OSM tracking map — DONE
21. Route polyline and ETA estimate — DONE
22. Realtime order subscription — DONE
23. Realtime rider-location subscription — DONE
24. Partner Centre — DONE
25. Restaurant partner application — DONE
26. Rider application — DONE
27. Application status/history — DONE
28. Admin restaurant/rider review foundation — DONE
29. Admin operational dashboard foundation — DONE
30. Customer coupons/reviews/support/notifications UI layer — DONE
31. Rider earnings/payout UI/backend layer — DONE
32. Payment status/webhook foundation — DONE
33. Barmer service-radius validation foundation — DONE
34. Secure validation helpers — DONE
35. Health-check endpoint hardening — DONE
36. RLS/security hardening migration — DONE
37. KYC private Storage bucket — DONE
38. KYC MIME allow-list — DONE
39. KYC 5 MB file limit — DONE
40. KYC per-user folder isolation — DONE
41. Real browser KYC file upload — DONE
42. KYC uploaded-object verification — DONE
43. KYC application ownership binding — DONE
44. KYC duplicate submission protection — DONE
45. KYC status/review metadata — DONE
46. KYC 5-minute signed preview URL — DONE
47. Admin KYC approve/reject queue — DONE
48. KYC applicant notification + audit foundation — DONE
49. KYC frontend smoke checks/security checklist — DONE
50. Production release configuration and third-party credentials — RELEASE-GATE (Supabase project, map/payment/push credentials, Android signing/Play Console and legal compliance cannot be safely invented in GitHub)

## Batch result
The actual KYC upload flow is now repository-backed end-to-end: browser → private Storage → server verification → pending record → admin signed preview → approve/reject → notification/audit trail.

The remaining release-gated item is intentionally explicit. A GitHub code change cannot create real payment accounts, SMS/push credentials, production secrets, signing keys, legal approvals, or Play Console declarations.

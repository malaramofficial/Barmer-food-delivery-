# 🍽️ Barmer Food Delivery

A Barmer-only food delivery platform for customers, restaurants/hotels, delivery riders and administrators.

## Current build
The repository now contains a mobile-first working frontend prototype with:
- Barmer-only restaurant discovery
- Restaurant menus and cart
- Checkout with COD/test online payment choice
- Order history and status timeline
- Live tracking UI with restaurant, rider and customer markers
- Demo order progression from restaurant notification through delivery
- Discreet Careers / Partner with us entry
- Restaurant/hotel verification request form
- Rider verification request form
- Responsive customer UI

## Production architecture
The production version should connect the UI to a secure backend such as Supabase/Postgres:
- Auth + role-based authorization: customer, restaurant, rider, admin
- RLS for every tenant/user-owned record
- Private KYC/restaurant documents
- Realtime order notifications and rider dispatch
- Secure live GPS updates for active deliveries only
- Barmer service-area/geofence enforcement
- COD plus configurable online payment provider
- Admin approval before restaurants/riders become active
- Audit logs, support, reviews, coupons, commissions and payouts

## Order lifecycle
`placed → restaurant_notified → accepted → preparing → ready_for_pickup → rider_assigned → picked_up → on_the_way → delivered`

Rejected/cancelled orders are terminal states.

## Maps
The current UI includes a provider-neutral demo map. For production, configure a real map/routing provider and never put private API secrets in frontend code. Turn-by-turn navigation should be handed off to an installed navigation app unless a licensed navigation SDK is configured.

## Security
Admin authentication must be server-authorized. Never hard-code an admin password in JavaScript or expose service-role keys in the browser. Verification documents must remain private.

## Files
- `index.html` — application shell
- `styles.css` — responsive design system
- `app.js` — customer/order/partner/admin prototype flows

## Next production milestones
1. Supabase schema + Auth + RLS
2. Restaurant/rider applications and private storage
3. Realtime order notification and dispatch RPC
4. GPS tracking and real map provider
5. Payment gateway
6. Admin moderation and analytics
7. Automated testing, PWA install/offline handling and production deployment

# Barmer Food Delivery — Engineering Audit & Refactor Plan

Date: 2026-09-08

## Current assessment

The repository has a substantial implementation across the customer web/PWA flow, native Android client, Supabase integration, KYC, rider dispatch/tracking, administration, and CI. The 50-task batch records these areas as implemented, while production credentials and external operational configuration remain intentionally release-gated.

## Architecture risks to address

1. The customer web shell still loads a large set of JavaScript files globally, increasing coupling and making ownership of state and UI behavior difficult to reason about.
2. The legacy `app.js` contains demo customer data and UI behavior alongside production integration hooks. Demo fallbacks should be isolated from production services.
3. CSS is difficult to maintain because the primary stylesheet is heavily compressed; design tokens and component-level sections should be made explicit.
4. The web app needs a single source of truth for authentication/session, API errors, loading states, and order state rather than parallel demo/localStorage and backend behavior.
5. Admin, restaurant, rider and customer capabilities should have clear authorization boundaries at both UI and backend layers.
6. External configuration must remain environment-driven; no service-role, payment, signing, or other private credentials should enter source control.

## Refactor target

### Customer
Browse → search/location → restaurant → menu → cart → checkout → tracking → order history.

### Restaurant
Application/approval → profile → menu → incoming orders → accept/reject → preparation → ready for pickup → earnings/settlement view.

### Rider
Application/approval → online/offline → available jobs → accept/skip → pickup → navigation → delivery confirmation → earnings.

### Admin
Authentication → operational dashboard → restaurant/rider/KYC moderation → live orders/riders → support → coupons/configuration → audit log.

### Platform services
Supabase Auth, Postgres/RLS, Edge Functions, private KYC Storage, Realtime, notifications, maps/geocoding, payments/webhooks, and monitoring.

## Implementation rule

Refactoring must preserve working production/security paths. New UI or architecture must not bypass server-side authorization, order-price recalculation, RLS, private KYC storage, or signed payment/webhook validation.

## Immediate engineering priorities

- Separate demo fixtures from production data access.
- Introduce shared configuration/service utilities.
- Establish explicit loading, empty, error and offline states.
- Consolidate order-state rendering around the backend lifecycle.
- Improve mobile-first visual hierarchy and accessibility without weakening security.
- Keep Android CI environment-driven and fail clearly when required release configuration is absent.
- Verify build and runtime paths after each structural change.

## Release boundary

The repository can contain production-ready source code, but real Supabase/payment/maps/push credentials, signing keys, legal approvals, and Play Console configuration must remain external release inputs.

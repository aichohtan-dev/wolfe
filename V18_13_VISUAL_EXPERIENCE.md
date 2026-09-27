# Wolfe V18.13 — Visual Experience & Configurator

## Visual Content Studio
- Admin-managed hero/campaign visual records.
- IMAGE and VIDEO media support.
- Placement slots: HERO, COLLECTION_REEL, MOODBOARD.
- Title, subtitle, poster, CTA link, active state and sort order.
- Public hero renderer supports autoplay/muted/loop/playsInline video with poster fallback.

## Furniture Detail Configurator
- Product-level accessory options for HANDLE, KNOB, PULL and similar hardware.
- Transparent PNG/WebP overlay URL per accessory.
- Positioning with X/Y percentages and scale.
- Customer can select an accessory and preview it over the product image.
- Admin can create/list/soft-disable accessory overlays per product.
- Designed as a fast 2D-first foundation for a later WebGL/3D upgrade.

## Migration
- V24__visual_content_and_configurator.sql

## Verification
- Java source brace/structure check: PASS.
- TypeScript compiler invocation: dependency modules unavailable; no parser/syntax failure was identified.
- Maven runtime compilation: not certified because Maven Central DNS/network is unavailable in the current environment.
- Razorpay untouched.

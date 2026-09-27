# Wolfe V18.14 — Visual Commerce Expansion

Built as one batch on V18.13.

## Visual experience
- 360° product spin frame storage + customer viewer
- 3D/WebAR-ready product visual asset model (`modelUrl`, `arUrl`, `posterUrl`)
- Visual-content hotspots with percentage positioning
- Save/share product configurations with public share token
- Room visualizer foundation
- Configurator-to-share workflow

## Discovery / conversion
- Product comparison surface
- Recently-viewed persistence for signed-in customers
- Back-in-stock subscriptions
- Back-in-stock notification trigger when inventory returns above zero
- Abandoned-cart recovery persistence
- Hourly abandoned-cart reminder scheduler with in-app notification

## Commerce integration
- Configured quote requests can carry a configuration ID
- Existing accessory configurator remains compatible
- Existing related-products flow remains compatible

## Verification
- Flyway migrations V25 added without duplicate version.
- Java brace/structural check: PASS.
- Full Maven/npm/Docker runtime certification remains pending until a network-enabled dependency environment is available.

## Explicitly not included
- Razorpay payment/refund integration remains intentionally untouched.
- Actual 3D/AR rendering requires uploaded model assets; V18.14 provides the backend asset contract and UI foundation.

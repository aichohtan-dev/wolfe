# Wolfe Master Checkpoint V18.27

## Current state
V18.27 includes the V18.26 security/ops alignment plus the V18.27 order-validation fail-fast fix.

## V18.27 fix
- `OrderService.create()` now validates customer/delivery completeness before product lookup.
- `OrderServiceValidationTest.rejectsMissingDeliveryDetails()` now reaches the intended validation path.
- All known `CreateOrder` calls use 11 arguments.
- All known `OrderService.Item` calls use 4 arguments.

## Verification
- Source delimiter checks: PASS.
- Constructor arity checks: PASS.
- Validation-before-product-lookup check: PASS.
- Maven runtime: NOT CERTIFIED; Maven 3.9.11 download blocked by DNS for `repo.maven.apache.org`.
- Docker/E2E: NOT CERTIFIED.

## Rules
- No false green claims.
- Razorpay untouched.
- Preserve Flyway history.
- Server-side pricing remains authoritative.

# Wolfe V18.27 — Order Validation Fail-Fast Fix

## Fixed
- Moved customer/delivery completeness validation in `OrderService.create()` before product lookup and configuration/bundle resolution.
- Prevents invalid customer data from causing unnecessary product/database lookups.
- Fixes `OrderServiceValidationTest.rejectsMissingDeliveryDetails()` runtime assertion path.

## Regression checks
- `CreateOrder` constructor call sites: 11 arguments.
- `Item` constructor call sites: 4 arguments.
- Customer validation appears before first `products.findBySlug` call.
- Java delimiter/source-structure checks pass.

## Runtime status
- Maven runtime execution remains uncertified because the environment cannot resolve `repo.maven.apache.org`.
- No fake green result or generated dependency lockfile was used.

Razorpay remains intentionally untouched.

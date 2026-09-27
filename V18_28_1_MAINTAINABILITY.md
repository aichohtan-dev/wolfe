# Wolfe V18.28.1 — Maintainability Cleanup

## Scope

Behavior-preserving cleanup of the V18.28.1 source tree. Razorpay/payment-provider integration was not added or changed.

## Changes

- Formatted the 93 backend Java source files into consistent 4-space indentation and readable statement layout.
- Reformatted TypeScript/TSX using the TypeScript compiler printer; `App.tsx` and `api.ts` are now expanded into reviewable multi-line code.
- Added `.editorconfig` to establish consistent whitespace/line-ending conventions.
- Added `docs/CODE_STYLE.md` with Java and React/TypeScript maintainability rules.
- Added `OrderServiceTest` covering standard shipping threshold, standard shipping fee, default shipping method, and invalid shipping method.
- Removed generated `tsconfig.app.tsbuildinfo` from the release package.
- Preserved the V18.28 security changes, including the `HttpServletRequest` import fix, Redis rate limiting, validation error mapping, and OpenAPI default-off configuration.

## Verification

- Java parser: 93/93 production Java files parse successfully.
- TypeScript/TSX syntax: 0 parser errors across source files.
- `HttpServletRequest` import: present in `CustomerController.java`.
- Test source files: 3.
- Test methods: 8.

## Not certified in this environment

A real Maven test/package run, npm dependency-backed build, Docker build, PostgreSQL/Flyway E2E, and Redis runtime test remain pending until the dependency/network environment is available. This cleanup therefore does **not** claim a green runtime build.

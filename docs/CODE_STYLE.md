# Wolfe Code Style

## Purpose

Keep the codebase readable, reviewable, and safe to change without altering business behavior.

## Java

- 4-space indentation.
- One logical statement per line where practical.
- Keep imports explicit; avoid wildcard imports in new code.
- Keep controllers thin: request validation and orchestration belong in services when logic grows.
- Prefer named private methods over dense multi-operation expressions.
- Keep constructors readable; when dependency count grows, split responsibilities into focused services/controllers rather than hiding dependencies.
- Do not introduce payment-provider code unless explicitly requested.

## TypeScript / React

- 2-space indentation.
- Keep API access in `src/api.ts`; UI components should not duplicate request construction.
- Prefer small components and hooks over one large page component.
- Use explicit domain types for new API contracts; avoid introducing new `any` values.
- Keep rendering logic separate from data transformation and API calls.

## Verification

Every behavior change should be followed by formatting/static checks and, when the environment permits it, the real Maven/npm/Docker test pipeline. A source-only check must not be described as a green runtime build.

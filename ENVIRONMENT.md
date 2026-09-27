# Wolfe V18.4 Environment

## Required
- Node.js 22+
- npm 10+
- Java 21+
- PostgreSQL 16+
- Redis 7+
- Docker Desktop (optional, for the compose stack)

## Frontend

The repository pins the Node major version with `.nvmrc` and `.node-version`.
Install dependencies from the project root:

```bash
npm install
npm run build
```

`node_modules/` is intentionally not committed: it is platform-specific and must be produced by `npm install` on the target machine.

## Backend

Use the included Maven Wrapper from `backend/`:

```bash
cd backend
./mvnw test
./mvnw package
```

Windows PowerShell/cmd:

```text
cd backend
mvnw.cmd test
mvnw.cmd package
```

The wrapper downloads Apache Maven 3.9.11 into the user's Maven wrapper cache on first run.

## V18.7 production variables

- `WOLFE_FRONTEND_ORIGIN` — exact browser origin allowed by the API CORS policy.
- `DATABASE_URL` — PostgreSQL JDBC URL.
- `DATABASE_USERNAME` / `DATABASE_PASSWORD` — PostgreSQL credentials.
- `REDIS_URL` — Redis connection URL.
- `PORT` — API port (default 8080).

## Dependency installation
This repository pins frontend dependency versions in `package.json`. Run `npm install` to generate/update `package-lock.json` before using `npm ci` in a locked production workflow. The checked-in CI/container path uses `npm install` because this release archive intentionally does not embed `node_modules` or a generated lockfile.

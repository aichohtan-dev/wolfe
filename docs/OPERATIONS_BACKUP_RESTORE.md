# Wolfe Operations — Backup, Restore & Recovery

## Authoritative data

- PostgreSQL is the authoritative persistent application database.
- Redis is used for rate limiting and ephemeral application state and is persisted with AOF.
- Uploaded catalog/PDF media lives in Docker volumes `wolfe-catalog-imports` and `wolfe-catalog-published` and must be backed up separately when business-critical.

## PostgreSQL backup

```bash
./ops/backup-postgres.sh ./backups/postgres
```

The script creates a PostgreSQL custom-format dump and SHA-256 sidecar. Store backup artifacts outside the application host as well.

## PostgreSQL restore

Restore only during a controlled maintenance window:

```bash
CONFIRM_RESTORE=YES ./ops/restore-postgres.sh ./backups/postgres/wolfe-postgres-YYYYMMDDTHHMMSSZ.dump
```

If a `.sha256` sidecar exists, the restore script verifies it before importing.

After restore:

1. Start PostgreSQL and Redis.
2. Start the API and verify Flyway migration state.
3. Verify `/actuator/health` and readiness.
4. Verify login, catalog reads, checkout, and order history with a non-production test account.
5. Check retailer settlement and return records before reopening production traffic.

## Media backup

Back up these Docker volumes with the hosting platform's volume/object-storage backup mechanism:

- `wolfe-catalog-imports`
- `wolfe-catalog-published`

A database-only restore does not restore these files.

## Recovery target

Production deployment must define an explicit RPO/RTO with the hosting provider. This repository provides the backup/restore mechanism and verification procedure but does not claim a production RPO/RTO without the actual storage and scheduling configuration.

## Health and observability

- `/actuator/health` is public for load-balancer health checks.
- `/actuator/health/liveness` and `/actuator/health/readiness` are enabled.
- `/actuator/info` is restricted to ADMIN/SUPER_ADMIN.
- API exceptions are normalized; unexpected errors are logged server-side without exposing internals.
- Docker healthchecks cover PostgreSQL, Redis, API, and frontend.

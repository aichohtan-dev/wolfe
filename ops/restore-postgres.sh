#!/usr/bin/env bash
set -euo pipefail
if [ "$#" -ne 1 ]; then echo "Usage: $0 <backup.dump>" >&2; exit 2; fi
BACKUP="$1"; [ -f "$BACKUP" ] || { echo "Backup not found: $BACKUP" >&2; exit 1; }
[ "${CONFIRM_RESTORE:-}" = "YES" ] || { echo "Set CONFIRM_RESTORE=YES to continue" >&2; exit 2; }
if [ -f "$BACKUP.sha256" ]; then
  if command -v sha256sum >/dev/null 2>&1; then sha256sum -c "$BACKUP.sha256"; else shasum -a 256 -c "$BACKUP.sha256"; fi
fi
docker compose exec -T postgres pg_restore --clean --if-exists --no-owner --no-privileges --exit-on-error --single-transaction -U "${POSTGRES_USER:-wolfe}" -d "${POSTGRES_DB:-wolfe}" < "$BACKUP"
echo "Restore complete"

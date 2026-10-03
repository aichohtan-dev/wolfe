#!/usr/bin/env bash
set -euo pipefail
umask 077
OUT_DIR="${1:-./backups/postgres}"
TIMESTAMP="$(date -u +%Y%m%dT%H%M%SZ)"
mkdir -p "$OUT_DIR"
FILE="$OUT_DIR/wolfe-postgres-$TIMESTAMP.dump"
echo "Creating PostgreSQL backup: $FILE"
docker compose exec -T postgres pg_dump -U "${POSTGRES_USER:-wolfe}" -d "${POSTGRES_DB:-wolfe}" -Fc > "$FILE"
[ -s "$FILE" ] || { rm -f "$FILE"; echo "Backup file is empty" >&2; exit 1; }
if command -v sha256sum >/dev/null 2>&1; then sha256sum "$FILE" > "$FILE.sha256"; else shasum -a 256 "$FILE" > "$FILE.sha256"; fi
echo "Backup complete: $FILE"

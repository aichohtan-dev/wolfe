#!/usr/bin/env bash
set -euo pipefail
umask 077
OUT_DIR="${1:-./backups}"
RETENTION_DAYS="${BACKUP_RETENTION_DAYS:-30}"
ENCRYPTION_KEY="${WOLFE_BACKUP_KEY:-}"
[ -n "$ENCRYPTION_KEY" ] || { echo 'Set WOLFE_BACKUP_KEY for encrypted backups.' >&2; exit 2; }
mkdir -p "$OUT_DIR/postgres" "$OUT_DIR/media"
./ops/backup-postgres.sh "$OUT_DIR/postgres"
./ops/backup-media.sh "$OUT_DIR/media"
for f in "$OUT_DIR"/postgres/*.dump "$OUT_DIR"/media/*.tar.gz; do
  [ -f "$f" ] || continue
  openssl enc -aes-256-cbc -pbkdf2 -salt -in "$f" -out "$f.enc" -pass env:WOLFE_BACKUP_KEY
  rm -f "$f" "$f.sha256"
  sha256sum "$f.enc" > "$f.enc.sha256"
done
find "$OUT_DIR" -type f -mtime "+$RETENTION_DAYS" -delete
if [ -n "${BACKUP_OFFSITE_DIR:-}" ]; then
  mkdir -p "$BACKUP_OFFSITE_DIR"
  cp -a "$OUT_DIR"/. "$BACKUP_OFFSITE_DIR"/
fi
if [ -n "${BACKUP_OFFSITE_RCLONE_REMOTE:-}" ] && command -v rclone >/dev/null 2>&1; then
  rclone copy "$OUT_DIR" "$BACKUP_OFFSITE_RCLONE_REMOTE" --immutable
fi
echo "Encrypted backup set complete: $OUT_DIR"

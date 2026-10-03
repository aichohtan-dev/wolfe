#!/usr/bin/env bash
set -euo pipefail
[ "$#" -eq 1 ] || { echo "Usage: $0 <backup-file.enc>" >&2; exit 2; }
[ "${CONFIRM_RESTORE:-}" = YES ] || { echo 'Set CONFIRM_RESTORE=YES to continue' >&2; exit 2; }
[ -n "${WOLFE_BACKUP_KEY:-}" ] || { echo 'Set WOLFE_BACKUP_KEY.' >&2; exit 2; }
ENC="$1"; TMP="$(mktemp)"; trap 'rm -f "$TMP"' EXIT
openssl enc -d -aes-256-cbc -pbkdf2 -in "$ENC" -out "$TMP" -pass env:WOLFE_BACKUP_KEY
case "$ENC" in
  *.dump.enc) CONFIRM_RESTORE=YES ./ops/restore-postgres.sh "$TMP";;
  *.tar.gz.enc) echo 'Decrypted media archive; use restore-media.sh with the two decrypted archives.';;
  *) echo 'Unknown encrypted backup type' >&2; exit 2;;
esac

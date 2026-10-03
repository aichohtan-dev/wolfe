#!/usr/bin/env bash
set -euo pipefail
[ -n "${WOLFE_BACKUP_KEY:-}" ] || { echo 'Set WOLFE_BACKUP_KEY.' >&2; exit 2; }
[ "$#" -ge 1 ] || { echo 'Usage: verify-backup.sh <file.enc>...' >&2; exit 2; }
for enc in "$@"; do
  tmp="$(mktemp)"; trap 'rm -f "$tmp"' EXIT
  openssl enc -d -aes-256-cbc -pbkdf2 -in "$enc" -out "$tmp" -pass env:WOLFE_BACKUP_KEY
  case "$enc" in
    *.dump.enc) pg_restore --list "$tmp" >/dev/null;;
    *.tar.gz.enc) tar -tzf "$tmp" >/dev/null;;
    *) echo "Unknown backup type: $enc" >&2; exit 2;;
  esac
  rm -f "$tmp"; trap - EXIT
done
echo 'Encrypted backup verification passed.'

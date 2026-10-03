#!/usr/bin/env bash
set -euo pipefail
if [ "$#" -ne 2 ]; then echo "Usage: $0 <imports.tar.gz> <published.tar.gz>" >&2; exit 2; fi
[ "${CONFIRM_RESTORE:-}" = "YES" ] || { echo "Set CONFIRM_RESTORE=YES" >&2; exit 2; }
for f in "$@"; do
  [ -f "$f" ] || { echo "Missing backup: $f" >&2; exit 1; }
done
tmp="$(mktemp -d)"; trap 'rm -rf "$tmp"' EXIT
for f in "$@"; do
  tar -tzf "$f" >/dev/null
done
docker compose cp "$1" api:/tmp/imports-restore.tar.gz
docker compose cp "$2" api:/tmp/published-restore.tar.gz
docker compose exec -T api sh -c 'rm -rf /app/public/catalog/imports /app/public/catalog/published && mkdir -p /app/public/catalog/imports /app/public/catalog/published && tar -xzf /tmp/imports-restore.tar.gz -C /app/public/catalog && tar -xzf /tmp/published-restore.tar.gz -C /app/public/catalog && rm -f /tmp/imports-restore.tar.gz /tmp/published-restore.tar.gz'
echo "Media restore complete"

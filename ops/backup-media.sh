#!/usr/bin/env bash
set -euo pipefail
umask 077
OUT_DIR="${1:-./backups/media}"
TIMESTAMP="$(date -u +%Y%m%dT%H%M%SZ)"
mkdir -p "$OUT_DIR"
for dir in imports published; do
  target="$OUT_DIR/catalog-$dir-$TIMESTAMP.tar.gz"
  docker compose exec -T api tar -C "/app/public/catalog" -czf - "$dir" > "$target"
  [ -s "$target" ] || { rm -f "$target"; echo "Empty media backup: $dir" >&2; exit 1; }
  if command -v sha256sum >/dev/null 2>&1; then sha256sum "$target" > "$target.sha256"; else shasum -a 256 "$target" > "$target.sha256"; fi
done
echo "Media backup complete: $OUT_DIR"

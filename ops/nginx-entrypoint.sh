#!/usr/bin/env sh
set -eu
: "${WOLFE_SERVER_NAME:=localhost}"
mkdir -p /etc/nginx/conf.d
sed "s|\${WOLFE_SERVER_NAME}|$(printf '%s' "$WOLFE_SERVER_NAME" | sed 's/[&|]/\\&/g')|g" /etc/nginx/templates/default.conf.template > /etc/nginx/conf.d/default.conf
exec nginx -g 'daemon off;'

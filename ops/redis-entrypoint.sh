#!/usr/bin/env sh
set -eu
ACL_FILE=/tmp/wolfe-users.acl
case "$REDIS_PASSWORD" in *[!A-Za-z0-9._-]*) echo "REDIS_PASSWORD must use URL/ACL-safe characters" >&2; exit 2;; esac
umask 077
printf 'user default off\nuser wolfe on >%s ~auth:rate:* +@read +@write +@connection +@scripting +info\n' "$REDIS_PASSWORD" > "$ACL_FILE"
exec redis-server --appendonly yes --maxmemory 256mb --maxmemory-policy noeviction --aclfile "$ACL_FILE"

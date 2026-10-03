# Wolfe backup policy

- `ops/backup-all.sh` creates PostgreSQL and media backups, encrypts each artifact with AES-256-CBC + PBKDF2 using `WOLFE_BACKUP_KEY`, applies `BACKUP_RETENTION_DAYS` (default 30), and can copy to `BACKUP_OFFSITE_DIR` or an `rclone` remote.
- Keep `WOLFE_BACKUP_KEY` outside the repository (secret manager/KMS in production). Never commit it.
- Test restore at least monthly using `CONFIRM_RESTORE=YES` on an isolated environment. Verify PostgreSQL checksum before restore.
- Media restore runs as the unprivileged API user; no root/chown operation is required.
- `ops/verify-backup.sh` validates encrypted PostgreSQL/media artifacts without mutating a database or media volume; a full isolated restore is still the final operational test.

## Public-form anti-abuse
Set `WOLFE_CAPTCHA_REQUIRED=true`, `WOLFE_CAPTCHA_SECRET=<Turnstile secret>` and `VITE_TURNSTILE_SITE_KEY=<Turnstile site key>` in production to enforce Cloudflare Turnstile. Rate limiting and honeypot checks remain active even when CAPTCHA is disabled for local development.

# Wolfe Admin Bootstrap

Wolfe does **not** expose a self-service endpoint for promoting a customer to `ADMIN`.
This is intentional: allowing a normal customer session to grant admin privileges would create an escalation path.

## One-time production procedure

1. Start the production application with the required secrets configured.
2. Register the initial operator through the normal customer registration flow.
3. Verify the operator email and account out-of-band.
4. Using an authenticated DBA/operations connection to the production PostgreSQL database, promote only that known account:

```sql
UPDATE customers
SET role = 'ADMIN'
WHERE lower(email) = lower('admin@example.com');
```

5. Verify exactly one intended account has the `ADMIN` role:

```sql
SELECT id, email, role
FROM customers
WHERE lower(email) = lower('admin@example.com');
```

6. Log in again so the application observes the updated database role for the new session. The API authorization layer resolves the current role from the database; the JWT is not the authoritative role source.
7. Verify `/api/v1/admin/**` is accessible with the new admin session and returns `403`/`401` for a normal customer session.

## Operational rules

- Do not add a public `promote-to-admin` endpoint.
- Do not put an admin password or JWT secret in migrations, source code, Docker Compose, or README files.
- Record the promotion in the organization's database-change/audit process.
- Keep the number of `ADMIN` accounts minimal and review them periodically.

## Production security defaults
- Keep `WOLFE_OPENAPI_ENABLED=false` unless API documentation is intentionally exposed in a controlled environment.
- Redis is required by authentication rate limiting; keep Redis on the private application network.

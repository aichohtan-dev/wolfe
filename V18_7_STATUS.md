# Wolfe V18.7 Verification Status

## Verified in archive
- ZIP integrity
- JSON syntax for package configuration
- Flyway migration numbering has no duplicate versions
- No obvious live payment/private-key secrets found by source scan
- Maven Wrapper files present
- Node 22 version pinning present
- CI and Docker configuration present

## Environment blockers
- No network/DNS access to npm registry in the current execution environment.
- No Maven installation and Maven Central is unreachable from the current environment.
- Therefore `npm install`, frontend production build, Maven tests and Docker image builds cannot be certified green here.

## Important release note
- `package-lock.json` is not present in this archive. CI/container use `npm install` rather than `npm ci` so the archive remains installable without a lockfile. For strict reproducibility, generate and commit the lockfile on an internet-enabled development/CI environment.
- Razorpay/payment gateway remains intentionally deferred.


## V18.8
Commerce shipping layer added: server-side shipping calculation, persisted shipping fields, shipping quote endpoint, and checkout display.

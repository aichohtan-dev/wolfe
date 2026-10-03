# Wolfe Missing File Reconciliation

**Date**: 2026-10-02  
**Reference Archive**: `f:\wolfe.zip` / `f:\wolfe1.zip` (Pre-audit historical snapshots)  
**Authoritative Workspace**: `f:\wolfe` (Current live production-ready working copy)  
**Policy**: The old ZIP is BACKUP ONLY. Files from the old ZIP must NOT be restored blindly.

---

## 1. Summary of Reconciliation Analysis

A deep index comparison was performed between the old ZIP backup archive (`f:\wolfe.zip`) and the current live workspace (`f:\wolfe`).

### Key Findings:
- The old backup archive dates back to an early snapshot (September 2026).
- The old backup only contained database migrations up to **`V29`**, whereas the current live workspace has **`V1` through `V62`** incorporating all deep security, concurrency, optimistic locking, money integrity, and settlement audit fixes.
- The old backup contained only 6 basic unit test files, whereas the current workspace contains 21 comprehensive contract and concurrency test suites.
- The old backup included an un-audited `node_modules` tree.

---

## 2. Granular Missing File Determination Matrix

| File / Component Path in Old Backup | Exists in Live Workspace? | Category | Determination & Justification | Action Taken |
| :--- | :--- | :--- | :--- | :--- |
| `wolfe/backend/target/*` | No | Build Artifacts | **Obsolete / Compiled binaries**. Generated automatically by `mvnw test-compile` / `mvnw package`. | **Do NOT restore.** |
| `wolfe/node_modules/*` | No | Dependency Tree | **Development Dependencies**. Should be cleanly installed via `npm ci --ignore-scripts` to prevent supply-chain drift. | **Do NOT restore.** |
| `wolfe/backend/target/classes/db/migration/V1..V29` | Yes (in source `src/main/resources/db/migration`) | Database Migrations | **Preserved & Expanded**. Live workspace has complete sequence `V1`–`V62`. | **No restore needed.** |
| `wolfe/src/components/*` | Yes | Frontend Components | **Preserved & Hardened**. Live components contain full cookie authentication, CSRF handling, and consent banner. | **No restore needed.** |
| `wolfe/ops/backup-*.sh`, `restore-*.sh` | Yes | Operations Tooling | **Present & Verified**. Live scripts contain modern backup and restore pipelines. | **No restore needed.** |
| `wolfe/patch.py` | Yes | Migration Utility | **Obsolete Legacy Tool**. Retained for audit history but not used in production runtime. | **Retained as-is.** |

---

## 3. Deliberately NOT Restored Files

1. **Old pre-compiled `.jar` / `.class` files from `backend/target/`**:
   - *Reason*: Stale bytecode from pre-audit iterations. Clean compilation from source with Java 21 is required.
2. **Old `node_modules` directory from backup zip**:
   - *Reason*: Incompatible binary bindings and potential lockfile divergence. `package-lock.json` governs reproducible installation.
3. **Old `application.yml` with hardcoded fallback passwords**:
   - *Reason*: Old backup contained dangerous default database passwords. The current workspace enforces strict environment-driven secrets.

---

## 4. Conclusion

The current workspace `f:\wolfe` is complete, authoritative, and supersedes all contents of `f:\wolfe.zip`. No files from the old backup require restoration.

# Wolfe Runtime Certification Plan

**Target Application**: Wolfe Omnichannel E-Commerce & Retailer Fulfillment Platform  
**Certification Standard**: Zero-Gap Runtime Assurance & Automated Test Harness  
**Scope**: Full end-to-end runtime validation across infrastructure, backend, database, cache, proxy, browser, security, and recovery layers.

---

## 1. Test Pillars Overview

- **R1: Infrastructure & Orchestration** (Docker Compose, Healthchecks, Memory Limits, Network isolation)
- **R2: Backend Application Core** (Spring Boot 3, Java 21, Actuator, Flyway migrations V1–V62)
- **R3: Database & Transactional Integrity** (PostgreSQL 16, Lock Ordering, Idempotency, Money constraints)
- **R4: Distributed Session & Cache** (Redis 7, Refresh Token Rotation, Session revoking, Rate limiting)
- **R5: Edge Proxy & Transport Security** (Nginx, CSP, Security Headers, CORS, Cookie SameSite)
- **R6: Frontend & Browser Experience** (React 18, Vite, Storage Consent, Contract tests, SEO Prerender)
- **R7: Disaster Recovery & Continuity** (PostgreSQL Dump/Restore, Volume persistence, Migration check)

---

## 2. Detailed Test Matrix

### 🔐 AUTHENTICATION & SESSION MANAGEMENT (R2, R4)
| Test ID | Test Scenario | Verification Method | Expected Outcome |
| :--- | :--- | :--- | :--- |
| **AUTH-01** | Customer Registration | `POST /api/v1/customers/register` | Generic 202 response, unverified status, BCrypt hashing. |
| **AUTH-02** | Customer Login (Valid) | `POST /api/v1/customers/login` | Returns HTTP-only Secure Access & Refresh cookies, resets failure rate limits. |
| **AUTH-03** | Invalid Login Probe | `POST /api/v1/customers/login` (bad password) | 401 Unauthorized, dummy BCrypt execution to prevent user enumeration timing attacks. |
| **AUTH-04** | Expired / Invalid JWT Access | `GET /api/v1/customers/sessions` (expired/tampered cookie) | 401 Unauthorized, no stack trace leak. |
| **AUTH-05** | Refresh Token Rotation | `POST /api/v1/customers/refresh` | Issues new Access + Refresh token pair; old refresh token invalidated. |
| **AUTH-06** | Refresh Token Reuse Attack | `POST /api/v1/customers/refresh` (replayed old token) | Immediate revocation of all family sessions, 401 Unauthorized, security audit event logged. |
| **AUTH-07** | Session Revocation / Logout | `POST /api/v1/customers/logout` | Clears cookies, increments customer session version, purges session from Redis. |

---

### 🛡️ SECURITY & AUTHORIZATION CONTRACTS (R5)
| Test ID | Test Scenario | Verification Method | Expected Outcome |
| :--- | :--- | :--- | :--- |
| **SEC-01** | Cross-Origin Request Isolation | Options preflight from unauthorized origin | 403 Forbidden / Origin header rejected. |
| **SEC-02** | Customer IDOR Isolation | Accessing `/api/v1/orders/{id}` of another customer | 403 Forbidden / Access Denied. |
| **SEC-03** | Retailer Multi-Tenant Boundary | Retailer A attempting to pack Retailer B order | 403 Forbidden / Order assignment mismatch. |
| **SEC-04** | Admin Authority Escalation | Non-admin caller hitting `/api/v1/admin/*` | 403 Forbidden (`PERM_ADMIN_*` check). |
| **SEC-05** | Rate Limiting Enforcement | Exceeding 10 login attempts within 60 seconds | 429 Too Many Requests. |
| **SEC-06** | Security Headers Verification | `curl -I http://localhost/` | `Content-Security-Policy`, `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`. |

---

### 🛍️ COMMERCE & CONFIGURATOR (R2, R3)
| Test ID | Test Scenario | Verification Method | Expected Outcome |
| :--- | :--- | :--- | :--- |
| **COMM-01** | Catalog & Variant Resolution | `GET /api/v1/catalog/products/{slug}` | Returns product entity with active variants and pricing. |
| **COMM-02** | Configurator State Persistence | `POST /api/v1/experience/configurations` | Generates share token, persists custom material/finish JSON. |
| **COMM-03** | Cart Line Pricing Invariant | `POST /api/v1/cart/items` | Prevents negative prices, recalculates server-side subtotal. |
| **COMM-04** | Coupon Application & Limits | `POST /api/v1/coupons/apply` | Enforces minimum purchase amount, expiry date, and usage limits. |
| **COMM-05** | Order Checkout Idempotency | `POST /api/v1/orders/checkout` with same Idempotency-Key | Exactly one order created; subsequent requests return cached order view. |

---

### 📦 INVENTORY & RETAILER FULFILLMENT NETWORK (R3)
| Test ID | Test Scenario | Verification Method | Expected Outcome |
| :--- | :--- | :--- | :--- |
| **INV-01** | Central Stock Reservation | Order confirmation trigger | Decrements available central stock, locks variant row optimistically. |
| **INV-02** | Concurrent Checkout Race | 50 simultaneous checkouts for 1 remaining stock unit | Exactly 1 success, 49 OUT_OF_STOCK failures, zero negative balance. |
| **INV-03** | Service Area Pincode Routing | Order with pincode 560001 | Matches nearest partner in service area with full stock. |
| **INV-04** | Retailer Order Acceptance | `POST /api/v1/retailer/orders/{id}/accept` | Sets status ACCEPTED, creates settlement record, releases central reservation. |
| **INV-05** | Retailer SLA Expiry Reallocation | Assignment unaccepted past SLA timeout | `expireStaleAssignments()` cancels assignment, releases retailer reservation, triggers re-allocation. |
| **INV-06** | Return & Restock Ledger | Multiple partial returns on order line | Backfilled return ledger maintains `returnedQuantity <= orderedQuantity`. |

---

### ⚙️ INFRASTRUCTURE & BACKUP (R1, R7)
| Test ID | Test Scenario | Verification Method | Expected Outcome |
| :--- | :--- | :--- | :--- |
| **INF-01** | Container Health Probes | `docker compose ps` / `/actuator/health` | `status: UP`, DB and Redis connection probes healthy. |
| **INF-02** | PostgreSQL Backup Dump | `bash ops/backup-postgres.sh` | Clean `.sql.gz` dump created without locking database. |
| **INF-03** | PostgreSQL Clean Restore | `bash ops/restore-postgres.sh` | Restores snapshot into clean database; Flyway confirms schema up to date. |
| **INF-04** | Backend Startup After Restore | Restart backend against restored database | Spring Boot starts cleanly; all entities validate successfully. |

---

## 3. Execution Commands & Verification Checklist

```bash
# 1. Frontend Checks
npm run test:contract      # Verifies security headers, auth cookie boundaries, storage consent
npm run security:scan      # Static code security rule scanner
node scripts/prerender-seo.mjs # SEO metadata generator

# 2. Backend Checks
./mvnw.cmd test-compile    # Compiles 159 source classes and 21 test suites
./mvnw.cmd test            # Runs unit and contract tests

# 3. Operations & Containers
docker compose config      # Validates Docker compose YAML and variable substitutions
docker compose up -d       # Launches isolated local runtime stack
```

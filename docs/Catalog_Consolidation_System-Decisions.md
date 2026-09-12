# Catalog Consolidation — Design Decisions

Concise ADR-style record of choices and trade-offs.

See also: [Catalog_Consolidation_System.md](Catalog_Consolidation_System.md) · [Catalog_Consolidation_System-Assessment.md](Catalog_Consolidation_System-Assessment.md)

---

## Architecture

### A1 — Two services (ingester + worker)

**Decision:** Ingester owns upload + dispatch; worker owns consolidation. Coupling is Kafka-only.

**Rationale:** Upload path stays fast and independently scalable; worker CPU/DB work does not block the API. Matches how marketplaces usually decouple intake from catalog mutation.

---

### A2 — Hexagonal layout

**Decision:** `application` / `dataprovider` / `entrypoint` / `infrastructure` in both services.

**Rationale:** Clear ports for tests and adapters. Heavier than MVC for this size, but keeps use cases free of Spring/JPA/Kafka details.

---

## Ingestion

### I1 — Async `202 Accepted`

**Decision:** Persist file + outbox in one transaction; return before parsing.

**Rationale:** Client is not held on large files; work continues via outbox relay.

---

### I2 — Receive vs dispatch split

**Decision:** `IngestFileUseCase` (store) and `ProductIngestUseCase` (stream + publish), triggered by relay.

**Rationale:** Separates fast ACK from slow streaming/Kafka work; one event per product simplifies partial failure and horizontal scale on the worker.

---

### I3 — Raw file in S3 (key = `ingestionId`)

**Decision:** Object key equals ingestion UUID.

**Rationale:** O(1) lookup for re-dispatch and audit; no path parsing.

---

### I4 — Transactional outbox

**Decision:** `product_ingest_outbox` written in the same TX as `ingestion_history`, not direct Kafka on upload.

**Rationale:** Survives broker outages; dispatch becomes retryable without losing the “file received” fact.

---

### I5 — Polling relay with row claim

**Decision:** Relay claims `PENDING` rows via `FOR UPDATE SKIP LOCKED`, sets `PROCESSING`, then dispatches.

**Rationale:** Simpler than CDC for a take-home; claim avoids double-dispatch with multiple ingester instances.

---

### I6 — Retain outbox rows after `DISPATCHED`

**Decision:** Status update, no delete on success.

**Rationale:** Drives poll API and debugging; TTL cleanup can come later.

---

### I7 — Slim `ingestion_history` (no batch table)

**Decision:** File-level history + outbox only; dropped `ingestion` / `ingestion_batch`.

**Rationale:** Batch state added little once each product is its own Kafka message and worker inbox holds per-entry outcomes.

---

### I8 — No mid-file checkpoint

**Decision:** One dispatch attempt streams the whole file; crash → re-stream; worker idempotency absorbs duplicates.

**Rationale:** Simpler outbox state machine. Acceptable while files are bounded; checkpoint would be next step at very large scale.

---

## Messaging

### M1 — One Kafka message per product

**Decision:** Topic `catalog.product-entry.update`, one record per array element.

**Rationale:** Failure isolation and parallel workers; avoids oversized messages and “replay the whole batch” semantics.

---

### M2 — `correlationId = {ingestionId}:{entryIndex}`

**Decision:** Stable id per slot in the file; worker inbox primary key.

**Rationale:** Retries and re-dispatch reuse the same id; index order is deterministic for a given file bytes.

---

### M3 — Topic naming `catalog.product-entry.update`

**Decision:** `{system}.{entity}.{verb}` convention.

**Rationale:** Readable in shared clusters; room for future catalog topics without collision.

---

### M4 — No worker → ingester callback

**Decision:** File status from outbox; per-product results in `product_entry_inbox` only.

**Rationale:** Avoids back-channel coupling and extra API surface; clients that need line-level detail can query inbox or add a read API later.

---

## Worker consolidation

### W1 — Handler chain

**Decision:** Validation → Upsert → Link; wired in a factory (not Spring bean chain).

**Rationale:** SRP per step; easy to read and test in order.

---

### W2 — `ProductEntryUseCase` orchestration

**Decision:** Redis lock → inbox claim → chain → inbox complete.

**Rationale:** Lock serializes concurrent work on the same catalog identity; inbox gives durable, queryable idempotency per event.

---

### W3 — Four outcomes

**Decision:** `CREATED`, `LINKED`, `ALREADY_LINKED`, `REJECTED`.

**Rationale:** Covers new catalog row, match to existing SKU, duplicate seller listing, and validation failure without overloading HTTP/upload status.

---

## Product matching

### P1 — SKU = canonical `brand#name`

**Decision:** Lowercase, strip diacritics, punctuation → hyphens (`StringHelper.sku`).

**Rationale:** Deterministic, cheap, same rule as SQLite seed migration. Good enough for the assessment; not fuzzy matching.

---

### P2 — Persisted `products.sku` + unique index

**Decision:** SKU column with `UNIQUE` constraint; match before insert.

**Rationale:** DB enforces dedup under concurrency; re-fetch on unique violation handles races.

---

### P3 — Assessment ambiguities

**Decision:** Match on brand + name only; ignore category for dedup; allow null brand; same seller may have multiple seller product ids pointing at one catalog product.

**Rationale:** Assessment says duplicates are undesirable at product level, not seller-listing level. Category/attribute matching would need explicit product rules we do not have.

---

### P4 — Schema beyond SQLite

**Decision:** Postgres `products` / `products_sellers` (snake_case); SQLite source unchanged in `docs/database/catalog.db`.

**Rationale:** JPA and Postgres conventions aligned; ingestion/inbox tables added for async pipeline.

---

## Idempotency & concurrency

### C1 — Inbox PK = `correlation_id`

**Decision:** One inbox row per Kafka event instance.

**Rationale:** Exact dedup for at-least-once delivery and re-dispatch of the same file slot.

---

### C2 — `idempotency_key` (indexed, not unique)

**Decision:** `seller#sku`; multiple inbox rows can share it across uploads.

**Rationale:** Correlation id distinguishes events; idempotency key defines **lock scope** for catalog writes, not inbox uniqueness.

---

### C3 — Dedup on completed `correlation_id` only

**Decision:** Skip processing if inbox row exists and is completed; in-flight rows may be retried.

**Rationale:** Completed = terminal success path; null/partial status allows Kafka redelivery to finish or fail visibly.

---

### C4 — Redis lock on `idempotency_key` (worker only)

**Decision:** 30s TTL, no renewal; ingester has no file-level lock.

**Rationale:** Per-message consolidation is short; lock prevents parallel double-create on the same SKU. File dispatch duration is a separate concern (streaming + Kafka timeouts).

---

### C5 — Nullable inbox `status` while processing

**Decision:** `status` null between claim and complete.

**Rationale:** Distinguishes in-flight from terminal states without a separate `PROCESSING` enum on the worker side.

---

## Infrastructure

### F1 — SQLite seed → Postgres

**Decision:** `docs/database/catalog.db` → `migrate_sqlite.py` → `02-catalog-seed.sql` on `reset-db`.

**Rationale:** Preserve assessment artifact; runtime is Postgres-only.

---

### F2 — Redpanda

**Decision:** Kafka-compatible broker in Docker.

**Rationale:** Single container, no ZooKeeper; API-compatible for Spring Kafka.

---

### F3 — Shared Postgres

**Decision:** Ingester and worker use the same database (different tables).

**Rationale:** Minimal ops for the exercise. Production might split catalog DB from ingestion metadata or use read replicas.

---

### F4 — Docker Compose at repo root

**Decision:** `docker compose up` from root; Makefile wraps common commands.

**Rationale:** Standard tooling first; Make optional for ingest/reset-db shortcuts.

---

## API & observability

### O1 — Poll `GET /api/v1/ingestions/{id}`

**Decision:** File-level status from outbox: `PENDING` → `PROCESSING` → `DISPATCHED` | `FAILED`.

**Rationale:** Matches client need (“is my file accepted/dispatched?”) without waiting for every product.

---

### O2 — No worker HTTP API for per-product results

**Decision:** Query `product_entry_inbox` (or add a read API later).

**Rationale:** YAGNI for take-home; inbox is already the audit log.

---

## Testing & scope

### T1 — Unit tests with mocked gateways

**Decision:** Mockito-only use case/gateway tests; JaCoCo ≥90% instruction coverage on both services.

**Rationale:** Fast feedback on business rules without Testcontainers cost; adapters stay thin.

---

### T2 — Intentionally untested

**Decision:** No full integration/E2E (Postgres + Kafka + Redis + S3); no load/concurrency suite.

**Rationale:** Time-boxed assessment; unit tests cover orchestration; edge-case fixture documents manual scenarios.

---

### T3 — Explicit non-goals

**Decision:** No auth, multi-tenant isolation, fuzzy matching, DLQ, distributed tracing, pre-validation pass on upload, S3/DB compensation, or mid-file checkpoint.

**Rationale:** Demonstrate core pipeline and trade-offs, not production completeness.

---

### T4 — First production hardening steps

**Decision (priority order):** (1) strict JSON array validation + row pre-validation before publish, (2) integration tests, (3) stuck `PROCESSING` outbox recovery, (4) observability (trace id = `correlationId`), (5) seller-link insert race handling mirroring product re-fetch.

**Rationale:** Closes known partial-failure and operability gaps before scale-out.

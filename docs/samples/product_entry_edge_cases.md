# Product entry edge cases — intake fixture

Small deterministic catalog file for manual testing and interview demos. Each row targets one consolidation behavior.

Upload:

```bash
curl -F "file=@ms-catalog-consolidation-ingester/data/samples/product_entry_edge_cases.json" \
  http://localhost:9090/api/v1/ingestions
```

Or from repo root:

```bash
make ingest-edge-cases
```

Poll until outbox status is `DISPATCHED`, then inspect the worker inbox and catalog tables.

---

## Verification queries

Replace `{ingestion_id}` with the id returned from the upload.

```sql
-- Per-entry outcomes (correlation_id = {ingestion_id}:{index})
SELECT correlation_id, status, reason, idempotency_key
FROM product_entry_inbox
WHERE ingestion_id = '{ingestion_id}'
ORDER BY correlation_id;

-- Seller links created by this fixture
SELECT ps.seller_name, ps.seller_product_id, ps.product_id, p.sku, p.name
FROM products_sellers ps
JOIN products p ON p.id = ps.product_id
WHERE ps.seller_name LIKE 'EdgeSeller%'
ORDER BY ps.seller_name, ps.seller_product_id;

-- Novel products introduced by this fixture
SELECT id, sku, name, brand
FROM products
WHERE sku LIKE 'edgebrand#%' OR sku LIKE 'testco#%' OR sku LIKE '#edge-case%'
   OR sku LIKE 'breville#%' OR name LIKE 'Edge Case%';
```

---

## Scenario table (first ingest)

| Index | Scenario | Seller | Seller product id | Expected status | Interview note |
|------:|----------|--------|-------------------|-----------------|----------------|
| 0 | Baseline LINKED to seed | EdgeSellerA | `…1101` | `LINKED` | SKU matches seeded Samsung Galaxy S23 |
| 1 | Baseline CREATED (novel) | EdgeSellerA | `…1102` | `CREATED` | New catalog row + seller link |
| 2 | Same seller, different id, same SKU | EdgeSellerB | `…2201` | `LINKED` | Second internal listing → same catalog product |
| 3 | Same seller, duplicate listing (pair of #2) | EdgeSellerB | `…2202` | `LINKED` | Two seller_product_ids → one `products` row |
| 4 | Same seller, different catalog product | EdgeSellerB | `…2203` | `CREATED` | Same seller, different SKU (novel widget beta) |
| 5 | Duplicate seller+id in same file | EdgeSellerB | `…2201` (reuse #2) | `ALREADY_LINKED` | Validation short-circuit after #2 linked |
| 6 | Whitespace normalization | EdgeSellerC | `…3301` | `LINKED` | Trim/normalize → same SKU as seed |
| 7 | Diacritics / canonicalization | EdgeSellerC | `…3302` | `CREATED` | SKU: `breville#cafe-maker` |
| 8 | SKU collision probe (`A.B`) | EdgeSellerD | `…4401` | `CREATED` | SKU: `testco#ab-gadget` (punctuation stripped) |
| 9 | SKU collision pair (`AB`) | EdgeSellerD | `…4402` | `LINKED` | Same canonical SKU as #8 → links, no duplicate product |
| 10 | Invalid seller product id | EdgeSellerE | `not-a-uuid` | `REJECTED` | UUID validation in worker |
| 11 | Symbols-only product name | EdgeSellerE | `…5501` | Ingester `FAILED` | `DomainException` at dispatch; see observed run below |
| 12 | Null brand (allowed) | EdgeSellerF | `…6601` | Not dispatched if #11 present | Same file aborts before index 12 is published |

---

## Same seller, different seller product ids (entries 2–5)

`EdgeSellerB` uploads three distinct seller product ids:

| Index | Seller product id | Catalog intent | Result |
|------:|-------------------|----------------|--------|
| 2 | `…2201` | Samsung Galaxy S23 (seed SKU) | `LINKED` |
| 3 | `…2202` | Same product, different internal id | `LINKED` (same `product_id` as #2) |
| 4 | `…2203` | Novel widget beta | `CREATED` (new `products` row) |
| 5 | `…2201` (repeat) | Same as #2 | `ALREADY_LINKED` |

Schema allows multiple `(seller_name, seller_product_id)` rows pointing at the same `product_id`. Redis lock key is `seller#sku`, so entries 2 and 3 serialize on the same catalog SKU — safe for `findOrCreateProduct`.

---

## Second upload (replay idempotency)

Re-upload the **same file** after the first run completes:

| Indices | Expected on 2nd ingest |
|--------:|------------------------|
| 0–4, 6–9, 12 | `ALREADY_LINKED` (seller link already exists) |
| 5 | `ALREADY_LINKED` (still duplicate of #2 from first run) |
| 10 | `REJECTED` (still invalid UUID) |
| 11 | Same as first run (see observed behavior) |

Each upload gets a new `ingestion_id` and new `correlation_id` values, but validation detects existing seller links.

---

## Out of scope (separate future fixtures)

- Malformed JSON arrays (e.g. `[{...}, "garbage"]`) — see Codex review #1
- Concurrent load / lock TTL stress tests

---

## Observed behavior (local run)

Ingestion id `4601be2c-b930-4948-8d2e-9fe7c4014d18` after `make ingest-edge-cases`:

| Index | Observed status | Notes |
|------:|-----------------|-------|
| 0 | `LINKED` | Matches expected |
| 1 | `CREATED` | Matches expected |
| 2 | `LINKED` | Matches expected |
| 3 | `LINKED` | Same `product_id` as #2 (same SKU) |
| 4 | `CREATED` | Matches expected |
| 5 | `ALREADY_LINKED` | Matches expected (duplicate of #2 in same file) |
| 6 | `LINKED` | Whitespace normalized → seed SKU |
| 7 | `CREATED` | SKU `breville#cafe-maker` |
| 8 | `CREATED` | SKU `testco#ab-gadget` |
| 9 | `LINKED` | Same SKU as #8 — no duplicate `products` row |
| 10 | `REJECTED` | Invalid UUID at worker |
| 11 | _(no inbox row)_ | Ingester throws `DomainException: Product name must contain letters or numbers.` during dispatch |
| 12 | _(no inbox row)_ | Never published — dispatch aborted at #11 |

**Outbox:** `FAILED` after 5 attempts — `DomainException: Product name must contain letters or numbers.`

**Important:** indices 0–10 were published to Kafka **before** the file failed. Worker processed them; #11–12 did not run. This is a known gap (deferred pre-validation). To test #12 alone, remove entry #11 from the file or split into separate uploads.

### Testing entry 12 in isolation

Upload a one-entry file with only index 12, or temporarily comment out entries 11–12 in the JSON and add a second mini-fixture — expected: `CREATED` with SKU `#edge-case-no-brand-product`.

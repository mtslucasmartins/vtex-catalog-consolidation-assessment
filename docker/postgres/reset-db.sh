#!/bin/sh
set -eu

root=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
compose="docker compose -f $root/docker-compose.yml"

python3 "$root/docker/postgres/migrate_sqlite.py" \
    "$root/docs/database/catalog.db" \
    > "$root/docker/postgres/02-catalog-seed.sql"

# Stop writers so they cannot insert catalog rows during reset.
$compose stop ms-catalog-consolidation-ingester ms-catalog-consolidation-worker 2>/dev/null || true

$compose up -d --wait postgres
$compose exec -T postgres \
    psql -U catalog -d catalog -v ON_ERROR_STOP=1 \
    -f /docker-entrypoint-initdb.d/01-schema.sql
$compose exec -T postgres \
    psql -U catalog -d catalog -v ON_ERROR_STOP=1 \
    -f /docker-entrypoint-initdb.d/02-catalog-seed.sql

$compose start ms-catalog-consolidation-ingester ms-catalog-consolidation-worker 2>/dev/null || true

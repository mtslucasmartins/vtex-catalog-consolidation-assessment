COMPOSE := docker compose
SAMPLE  := ms-catalog-consolidation-ingester/data/samples/product-entry.json
EDGE_CASES := ms-catalog-consolidation-ingester/data/samples/product_entry_edge_cases.json

.PHONY: build up down logs infra run-ingester run-worker ingest ingest-edge-cases reset-db test-coverage

build:
	cd ms-catalog-consolidation-ingester && mvn -q package
	cd ms-catalog-consolidation-worker && mvn -q package

up:
	$(COMPOSE) up --build -d

down:
	$(COMPOSE) down

logs:
	$(COMPOSE) logs -f

infra:
	$(COMPOSE) up -d postgres redpanda localstack

run-ingester:
	cd ms-catalog-consolidation-ingester && mvn -q spring-boot:run

run-worker:
	cd ms-catalog-consolidation-worker && mvn -q spring-boot:run

ingest:
	curl -s -F "file=@$(SAMPLE)" http://localhost:9090/api/v1/ingestions

ingest-edge-cases:
	curl -s -F "file=@$(EDGE_CASES)" http://localhost:9090/api/v1/ingestions

reset-db:
	./docker/postgres/reset-db.sh

test-coverage:
	cd ms-catalog-consolidation-ingester && mvn -q verify
	cd ms-catalog-consolidation-worker && mvn -q verify

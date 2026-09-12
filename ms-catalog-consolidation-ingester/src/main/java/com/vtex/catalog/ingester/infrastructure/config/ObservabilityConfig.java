package com.vtex.catalog.ingester.infrastructure.config;

import com.vtex.catalog.ingester.dataprovider.repository.ProductIngestOutboxRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.vtex.catalog.ingester.application.common.metrics.MetricNames.PRODUCT_INGEST_OUTBOX;
import static com.vtex.catalog.ingester.dataprovider.table.ProductIngestOutboxTable.FAILED;
import static com.vtex.catalog.ingester.dataprovider.table.ProductIngestOutboxTable.PENDING;

@Configuration
public class ObservabilityConfig {

    @Bean
    MeterBinder productIngestOutboxMetrics(ProductIngestOutboxRepository repository) {
        return registry -> {
            Gauge.builder(PRODUCT_INGEST_OUTBOX, repository, value -> value.countByStatus(PENDING))
                    .tag("status", PENDING)
                    .register(registry);
            Gauge.builder(PRODUCT_INGEST_OUTBOX, repository, value -> value.countByStatus(FAILED))
                    .tag("status", FAILED)
                    .register(registry);
        };
    }
}

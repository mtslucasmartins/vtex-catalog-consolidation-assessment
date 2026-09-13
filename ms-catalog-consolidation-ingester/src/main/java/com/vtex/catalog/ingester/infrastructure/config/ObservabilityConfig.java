package com.vtex.catalog.ingester.infrastructure.config;

import com.vtex.catalog.ingester.dataprovider.repository.ProductIngestOutboxRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.vtex.catalog.ingester.application.domain.ingestion.IngestionDispatchStatus;

import static com.vtex.catalog.ingester.application.common.metrics.MetricNames.PRODUCT_INGEST_OUTBOX;

@Configuration
public class ObservabilityConfig {

    @Bean
    MeterBinder productIngestOutboxMetrics(ProductIngestOutboxRepository repository) {
        return registry -> {
            Gauge.builder(PRODUCT_INGEST_OUTBOX, repository,
                            value -> value.countByStatus(IngestionDispatchStatus.PENDING.name()))
                    .tag("status", IngestionDispatchStatus.PENDING.name())
                    .register(registry);
            Gauge.builder(PRODUCT_INGEST_OUTBOX, repository,
                            value -> value.countByStatus(IngestionDispatchStatus.FAILED.name()))
                    .tag("status", IngestionDispatchStatus.FAILED.name())
                    .register(registry);
        };
    }
}

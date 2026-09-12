package com.vtex.catalog.ingester.infrastructure.messaging;

import com.vtex.catalog.ingester.application.domain.ingestion.IngestionDispatchStatus;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionId;
import com.vtex.catalog.ingester.application.gateway.product.ProductIngestOutboxGateway;
import com.vtex.catalog.ingester.application.usecase.ProductIngestCommand;
import com.vtex.catalog.ingester.application.usecase.ProductIngestUseCase;
import com.vtex.catalog.ingester.infrastructure.config.IngesterProperties;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.vtex.catalog.ingester.application.common.metrics.MetricNames.PRODUCT_INGEST_JOBS;

/**
 * Starts pending ingestions. Rows are claimed atomically so multiple ingester instances
 * do not dispatch the same ingestion. Failures are retried up to {@code max-attempts}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelay {

    private final ProductIngestOutboxGateway outboxGateway;

    private final IngesterProperties properties;

    private final ProductIngestUseCase productIngestUseCase;

    private final MeterRegistry meterRegistry;

    @Scheduled(fixedDelayString = "${ingester.outbox.ingest-relay-delay-ms:100}")
    public void relayIngestions() {
        for (IngestionId ingestionId : outboxGateway.claimPending(properties.getOutbox().getIngestRelayBatchSize())) {
            try {
                productIngestUseCase.execute(ProductIngestCommand.from(ingestionId));
            } catch (Exception exception) {
                meterRegistry.counter(PRODUCT_INGEST_JOBS, "outcome", "failed").increment();
                recordFailure(ingestionId, exception);
            }
        }
    }

    private void recordFailure(IngestionId ingestionId, Exception exception) {
        var outbox = properties.getOutbox();
        Throwable cause = exception.getCause() != null ? exception.getCause() : exception;
        String reason = cause.getClass().getSimpleName() + ": " + cause.getMessage();
        outboxGateway.registerFailure(ingestionId, reason, outbox.getMaxAttempts());
        var status = outboxGateway.findStatusByIngestionId(ingestionId).orElse(null);
        if (status == IngestionDispatchStatus.FAILED) {
            log.error("Product ingest {} FAILED: {}", ingestionId, reason);
        } else {
            log.warn("Product ingest {} attempt failed: {}", ingestionId, reason);
        }
    }
}

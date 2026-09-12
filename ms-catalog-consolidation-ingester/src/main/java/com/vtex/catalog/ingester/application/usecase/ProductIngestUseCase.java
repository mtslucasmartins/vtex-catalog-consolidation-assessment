package com.vtex.catalog.ingester.application.usecase;

import com.vtex.catalog.ingester.application.common.exception.IngestionNotFoundException;
import com.vtex.catalog.ingester.application.common.exception.InvalidIngestionFileException;
import com.vtex.catalog.ingester.application.common.stereotypes.UseCase;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionHistory;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionId;
import com.vtex.catalog.ingester.application.gateway.history.IngestionHistoryGateway;
import com.vtex.catalog.ingester.application.gateway.product.ProductEntryEventGateway;
import com.vtex.catalog.ingester.application.gateway.product.ProductEntryReaderGateway;
import com.vtex.catalog.ingester.application.gateway.product.ProductIngestOutboxGateway;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.vtex.catalog.ingester.application.common.metrics.MetricNames.PRODUCT_INGEST_JOBS;

/**
 * Streams a stored seller file and publishes one Kafka event per product entry.
 * Dispatch state lives on {@code product_ingest_outbox}; history is audit-only.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductIngestUseCase implements UseCase<ProductIngestCommand, Void> {

    private final IngestionHistoryGateway historyGateway;

    private final ProductIngestOutboxGateway outboxGateway;

    private final ProductEntryReaderGateway readerGateway;

    private final ProductEntryEventGateway eventGateway;

    private final MeterRegistry meterRegistry;

    @Override
    public Void execute(ProductIngestCommand command) {
        var ingestionId = command.getIngestionId();

        if (!outboxGateway.isDispatchable(ingestionId)) {
            logDispatchSkip(ingestionId);
            return null;
        }

        var history = this.historyGateway.findById(ingestionId)
                .orElseThrow(() -> new IngestionNotFoundException(ingestionId));

        try {
            var totalEntries = streamAndPublish(ingestionId);
            finishDispatch(ingestionId, history, totalEntries);
        } catch (InvalidIngestionFileException exception) {
            abortDispatch(ingestionId, exception);
        }

        return null;
    }

    private void logDispatchSkip(IngestionId ingestionId) {
        var status = outboxGateway.findStatusByIngestionId(ingestionId).orElse(null);
        log.info("Ingestion {} is {}; skipping dispatch", ingestionId, status);
    }

    private long streamAndPublish(IngestionId ingestionId) {
        return readerGateway.read(ingestionId.getValue(), (entryIndex, product) ->
                eventGateway.publish(ingestionId, entryIndex, product));
    }

    private void finishDispatch(IngestionId ingestionId, IngestionHistory history, long totalEntries) {
        outboxGateway.markDispatched(ingestionId);
        history.touch();
        historyGateway.save(history);
        meterRegistry.counter(PRODUCT_INGEST_JOBS, "outcome", "successful").increment();
        log.info("Ingestion {} dispatched {} entries", ingestionId, totalEntries);
    }

    private void abortDispatch(IngestionId ingestionId, InvalidIngestionFileException exception) {
        log.warn("Ingestion {} failed: {}", ingestionId, exception.getMessage());
        outboxGateway.markFailed(ingestionId, exception.getMessage());
        meterRegistry.counter(PRODUCT_INGEST_JOBS, "outcome", "failed").increment();
    }
}

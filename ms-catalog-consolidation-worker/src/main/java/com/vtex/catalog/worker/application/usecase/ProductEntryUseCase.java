package com.vtex.catalog.worker.application.usecase;

import com.vtex.catalog.worker.application.common.exception.InvalidProductException;
import com.vtex.catalog.worker.application.common.stereotypes.UseCase;
import com.vtex.catalog.worker.application.domain.processing.ProductEntryIdempotencyKey;
import com.vtex.catalog.worker.application.domain.processing.ProductEntryInbox;
import com.vtex.catalog.worker.application.domain.processing.ProductEntryStatus;
import com.vtex.catalog.worker.application.gateway.DistributedLockGateway;
import com.vtex.catalog.worker.application.gateway.ProductEntryInboxGateway;
import com.vtex.catalog.worker.application.usecase.iterator.ProductEntryChainExecutor;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import static com.vtex.catalog.worker.application.common.metrics.MetricNames.PRODUCT_ENTRIES_PROCESSED;
import static com.vtex.catalog.worker.application.common.metrics.MetricNames.PRODUCT_ENTRY_HANDLED;
import static com.vtex.catalog.worker.application.common.metrics.MetricNames.PRODUCT_ENTRY_PROCESSING;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEntryUseCase implements UseCase<ProductEntryCommand, ProductEntryStatus> {

    private final ProductEntryChainExecutor chainExecutor;

    private final ProductEntryInboxGateway productEntryInboxGateway;

    private final DistributedLockGateway lockGateway;

    private final MeterRegistry meterRegistry;

    @Override
    public ProductEntryStatus execute(ProductEntryCommand command) {
        var startedAt = System.nanoTime();
        var identifiers = command.getIdentifiers();
        var idempotencyKey = ProductEntryIdempotencyKey.from(command.getProduct());

        try {
            Lock lock = lockGateway.acquire(idempotencyKey);

            try {
                var existing = productEntryInboxGateway.findByCorrelationId(identifiers.getCorrelationId());
                if (existing.isPresent() && existing.get().isCompleted()) {
                    recordHandled("duplicate");
                    return existing.get().getStatus();
                }

                productEntryInboxGateway.claim(
                        ProductEntryInbox.pending(identifiers, idempotencyKey));

                ProductEntryStatus status;
                String reason;
                try {
                    status = chainExecutor.execute(command.getProduct());
                    reason = outcomeReason(status);
                } catch (InvalidProductException exception) {
                    log.warn("Rejected product entry {}: {}", identifiers.getCorrelationId(), exception.getMessage());
                    status = ProductEntryStatus.REJECTED;
                    reason = exception.getMessage();
                }

                productEntryInboxGateway.complete(identifiers.getCorrelationId(), status, reason);
                recordProcessed(startedAt, status);
                return status;
            } finally {
                lock.unlock();
            }
        } catch (RuntimeException exception) {
            recordHandled("error");
            recordProcessingTime(startedAt, ProductEntryStatus.FAILED, "error");
            throw exception;
        }
    }

    private void recordProcessed(long startedAt, ProductEntryStatus status) {
        meterRegistry.counter(PRODUCT_ENTRIES_PROCESSED, "status", status.name()).increment();
        recordHandled("processed");
        recordProcessingTime(startedAt, status, "processed");
    }

    private void recordHandled(String outcome) {
        meterRegistry.counter(PRODUCT_ENTRY_HANDLED, "outcome", outcome).increment();
    }

    private void recordProcessingTime(long startedAt, ProductEntryStatus status, String outcome) {
        Timer.builder(PRODUCT_ENTRY_PROCESSING)
                .tags("status", status.name(), "outcome", outcome)
                .register(meterRegistry)
                .record(System.nanoTime() - startedAt, TimeUnit.NANOSECONDS);
    }

    private static String outcomeReason(ProductEntryStatus status) {
        return switch (status) {
            case CREATED -> "Consolidated as CREATED";
            case LINKED -> "Consolidated as LINKED";
            case ALREADY_LINKED -> "Consolidated as ALREADY_LINKED";
            case REJECTED -> "Rejected";
            case FAILED -> "Failed";
        };
    }
}

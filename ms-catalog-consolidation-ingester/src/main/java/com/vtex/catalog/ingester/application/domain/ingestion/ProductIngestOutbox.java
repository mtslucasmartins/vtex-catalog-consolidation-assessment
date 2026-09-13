package com.vtex.catalog.ingester.application.domain.ingestion;

import com.vtex.catalog.ingester.application.common.stereotypes.DomainEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class ProductIngestOutbox implements DomainEntity {

    private final String id;

    private final IngestionId ingestionId;

    private IngestionDispatchStatus status;

    private int attempts;

    private String failureReason;

    private IngestionChronology chronology;

    public static ProductIngestOutbox pending(IngestionId ingestionId) {
        return ProductIngestOutbox.builder()
                .id(UUID.randomUUID().toString())
                .ingestionId(ingestionId)
                .status(IngestionDispatchStatus.PENDING)
                .attempts(0)
                .chronology(IngestionChronology.now())
                .build();
    }

    public void claimForProcessing() {
        this.status = IngestionDispatchStatus.PROCESSING;
        this.chronology.touch();
    }

    public void registerFailure(String reason, int maxAttempts) {
        this.attempts++;
        this.failureReason = reason;
        this.chronology.touch();
        if (this.attempts >= maxAttempts) {
            this.status = IngestionDispatchStatus.FAILED;
        } else {
            this.status = IngestionDispatchStatus.PENDING;
        }
    }

    public void markDispatched() {
        this.status = IngestionDispatchStatus.DISPATCHED;
        this.chronology.touch();
    }

    public void markFailed(String reason) {
        this.status = IngestionDispatchStatus.FAILED;
        this.failureReason = reason;
        this.chronology.touch();
    }

    public boolean isFailed() {
        return status == IngestionDispatchStatus.FAILED;
    }

    public boolean isDispatchable() {
        return status == IngestionDispatchStatus.PENDING
                || status == IngestionDispatchStatus.PROCESSING;
    }
}

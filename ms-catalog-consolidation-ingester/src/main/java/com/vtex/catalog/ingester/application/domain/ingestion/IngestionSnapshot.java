package com.vtex.catalog.ingester.application.domain.ingestion;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/** Read model for API polling: history row + outbox dispatch status. */
@Getter
@Builder
public class IngestionSnapshot {

    private IngestionId id;

    private String fileName;

    private IngestionDispatchStatus status;

    private String failureReason;

    private Instant createdAt;

    private Instant updatedAt;
}

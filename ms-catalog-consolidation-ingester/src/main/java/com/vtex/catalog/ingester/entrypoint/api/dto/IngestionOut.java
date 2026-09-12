package com.vtex.catalog.ingester.entrypoint.api.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class IngestionOut {

    private final String id;
    private final String fileName;
    private final String status;
    private final String failureReason;
    private final Instant createdAt;
    private final Instant updatedAt;
}

package com.vtex.catalog.worker.application.domain.processing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.time.Instant;

@Data
@Builder
@ToString
@AllArgsConstructor
public class ProductEntryInbox {

    private ProductEntryIdentifiers details;

    private String idempotencyKey;

    private ProductEntryStatus status;

    private String reason;

    private Instant createdAt;

    private Instant updatedAt;

    public boolean isCompleted() {
        return status != null;
    }

    public static ProductEntryInbox pending(
            ProductEntryIdentifiers details,
            String idempotencyKey) {
        return ProductEntryInbox.builder()
                .details(details)
                .idempotencyKey(idempotencyKey)
                .build();
    }

    public static ProductEntryInbox completed(
            ProductEntryIdentifiers details,
            String idempotencyKey,
            ProductEntryStatus status,
            String reason) {
        return ProductEntryInbox.builder()
                .details(details)
                .idempotencyKey(idempotencyKey)
                .status(status)
                .reason(reason)
                .build();
    }
}

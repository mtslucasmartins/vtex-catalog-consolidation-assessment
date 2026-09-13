package com.vtex.catalog.worker.application.domain.processing;

import com.vtex.catalog.worker.application.common.stereotypes.DomainEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;

@Getter
@Builder(toBuilder = true)
@ToString
@AllArgsConstructor
public class ProductEntryInbox implements DomainEntity {

    private ProductEntryIdentifiers details;

    private String idempotencyKey;

    private ProductEntryStatus status;

    private String reason;

    private Instant createdAt;

    private Instant updatedAt;

    public boolean isCompleted() {
        return status != null;
    }

    public void complete(ProductEntryStatus status, String reason) {
        this.status = status;
        this.reason = reason;
        this.updatedAt = Instant.now();
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

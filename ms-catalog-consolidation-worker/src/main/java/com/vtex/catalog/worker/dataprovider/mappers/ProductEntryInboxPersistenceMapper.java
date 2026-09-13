package com.vtex.catalog.worker.dataprovider.mappers;

import com.vtex.catalog.worker.application.domain.processing.ProductEntryIdentifiers;
import com.vtex.catalog.worker.application.domain.processing.ProductEntryInbox;
import com.vtex.catalog.worker.application.domain.processing.ProductEntryStatus;
import com.vtex.catalog.worker.dataprovider.table.ProductEntryInboxTable;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ProductEntryInboxPersistenceMapper {

    public ProductEntryInboxTable toPendingEntity(ProductEntryInbox inbox) {
        var now = Instant.now();
        return toEntity(inbox.toBuilder()
                .createdAt(now)
                .updatedAt(now)
                .build());
    }

    public ProductEntryInboxTable toEntity(ProductEntryInbox inbox) {
        var identifiers = inbox.getDetails();
        return ProductEntryInboxTable.builder()
                .correlationId(identifiers.getCorrelationId())
                .idempotencyKey(inbox.getIdempotencyKey())
                .ingestionId(identifiers.getIngestionId())
                .status(inbox.getStatus() != null ? inbox.getStatus().name() : null)
                .reason(inbox.getReason())
                .createdAt(inbox.getCreatedAt())
                .updatedAt(inbox.getUpdatedAt())
                .build();
    }

    public ProductEntryInbox toDomain(ProductEntryInboxTable entity) {
        var identifiers = ProductEntryIdentifiers.from(entity.getCorrelationId(), entity.getIngestionId());
        var status = entity.getStatus() != null
                ? ProductEntryStatus.valueOf(entity.getStatus())
                : null;
        return ProductEntryInbox.builder()
                .details(identifiers)
                .idempotencyKey(entity.getIdempotencyKey())
                .status(status)
                .reason(entity.getReason())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

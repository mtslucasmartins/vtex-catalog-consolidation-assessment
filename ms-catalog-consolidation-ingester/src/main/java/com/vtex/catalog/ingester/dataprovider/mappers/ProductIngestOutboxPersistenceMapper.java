package com.vtex.catalog.ingester.dataprovider.mappers;

import com.vtex.catalog.ingester.application.domain.ingestion.IngestionChronology;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionDispatchStatus;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionId;
import com.vtex.catalog.ingester.application.domain.ingestion.ProductIngestOutbox;
import com.vtex.catalog.ingester.dataprovider.table.ProductIngestOutboxTable;
import org.springframework.stereotype.Component;

@Component
public class ProductIngestOutboxPersistenceMapper {

    public ProductIngestOutboxTable toEntity(ProductIngestOutbox outbox) {
        var chronology = outbox.getChronology();
        return ProductIngestOutboxTable.builder()
                .id(outbox.getId())
                .ingestionId(outbox.getIngestionId().getValue())
                .status(outbox.getStatus().name())
                .attempts(outbox.getAttempts())
                .failureReason(outbox.getFailureReason())
                .createdAt(chronology.getCreatedAt())
                .updatedAt(chronology.getUpdatedAt())
                .build();
    }

    public ProductIngestOutbox toDomain(ProductIngestOutboxTable entity) {
        return ProductIngestOutbox.builder()
                .id(entity.getId())
                .ingestionId(IngestionId.of(entity.getIngestionId()))
                .status(IngestionDispatchStatus.valueOf(entity.getStatus()))
                .attempts(entity.getAttempts())
                .failureReason(entity.getFailureReason())
                .chronology(IngestionChronology.builder()
                        .createdAt(entity.getCreatedAt())
                        .updatedAt(entity.getUpdatedAt())
                        .build())
                .build();
    }
}

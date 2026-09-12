package com.vtex.catalog.ingester.dataprovider.mappers;

import com.vtex.catalog.ingester.application.domain.ingestion.IngestionChronology;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionHistory;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionId;
import com.vtex.catalog.ingester.dataprovider.table.IngestionHistoryTable;
import org.springframework.stereotype.Component;

@Component
public class IngestionHistoryPersistenceMapper {

    public IngestionHistoryTable toEntity(IngestionHistory history) {
        var chronology = history.getChronology();
        return IngestionHistoryTable.builder()
                .id(history.getId().getValue())
                .fileName(history.getFileName())
                .createdAt(chronology.getCreatedAt())
                .updatedAt(chronology.getUpdatedAt())
                .build();
    }

    public IngestionHistory toDomain(IngestionHistoryTable entity) {
        return IngestionHistory.builder()
                .id(IngestionId.of(entity.getId()))
                .fileName(entity.getFileName())
                .chronology(IngestionChronology.builder()
                        .createdAt(entity.getCreatedAt())
                        .updatedAt(entity.getUpdatedAt())
                        .build())
                .build();
    }
}

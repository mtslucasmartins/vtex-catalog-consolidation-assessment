package com.vtex.catalog.ingester.dataprovider.mappers;

import com.vtex.catalog.ingester.application.domain.ingestion.IngestionChronology;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionHistory;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionId;
import com.vtex.catalog.ingester.dataprovider.table.IngestionHistoryTable;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IngestionHistoryPersistenceMapperTest {

    private final IngestionHistoryPersistenceMapper mapper = new IngestionHistoryPersistenceMapper();

    @Test
    void givenHistory_whenToEntity_thenMapsFields() {
        // Given
        var created = Instant.parse("2026-01-01T00:00:00Z");
        var updated = Instant.parse("2026-01-02T00:00:00Z");
        var history = IngestionHistory.builder()
                .id(IngestionId.of("ing-1"))
                .fileName("products.json")
                .chronology(IngestionChronology.builder().createdAt(created).updatedAt(updated).build())
                .build();

        // When
        var entity = mapper.toEntity(history);

        // Then
        assertEquals("ing-1", entity.getId());
        assertEquals("products.json", entity.getFileName());
        assertEquals(created, entity.getCreatedAt());
    }

    @Test
    void givenEntity_whenToDomain_thenMapsFields() {
        // Given
        var entity = IngestionHistoryTable.builder()
                .id("ing-1")
                .fileName("products.json")
                .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2026-01-02T00:00:00Z"))
                .build();

        // When
        var history = mapper.toDomain(entity);

        // Then
        assertEquals("ing-1", history.getId().getValue());
        assertEquals("products.json", history.getFileName());
        assertEquals(Instant.parse("2026-01-02T00:00:00Z"), history.getUpdatedAt());
    }
}

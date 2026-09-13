package com.vtex.catalog.ingester.dataprovider.mappers;

import com.vtex.catalog.ingester.application.domain.ingestion.IngestionChronology;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionDispatchStatus;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionId;
import com.vtex.catalog.ingester.application.domain.ingestion.ProductIngestOutbox;
import com.vtex.catalog.ingester.dataprovider.table.ProductIngestOutboxTable;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductIngestOutboxPersistenceMapperTest {

    private final ProductIngestOutboxPersistenceMapper mapper = new ProductIngestOutboxPersistenceMapper();

    @Test
    void givenOutbox_whenToEntity_thenMapsFields() {
        // Given
        var created = Instant.parse("2026-01-01T00:00:00Z");
        var updated = Instant.parse("2026-01-02T00:00:00Z");
        var outbox = ProductIngestOutbox.builder()
                .id("outbox-1")
                .ingestionId(IngestionId.of("ing-1"))
                .status(IngestionDispatchStatus.PENDING)
                .attempts(0)
                .chronology(IngestionChronology.builder().createdAt(created).updatedAt(updated).build())
                .build();

        // When
        var entity = mapper.toEntity(outbox);

        // Then
        assertEquals("outbox-1", entity.getId());
        assertEquals("ing-1", entity.getIngestionId());
        assertEquals("PENDING", entity.getStatus());
        assertEquals(created, entity.getCreatedAt());
    }

    @Test
    void givenEntity_whenToDomain_thenMapsFields() {
        // Given
        var entity = ProductIngestOutboxTable.builder()
                .id("outbox-1")
                .ingestionId("ing-1")
                .status("FAILED")
                .attempts(2)
                .failureReason("bad json")
                .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2026-01-02T00:00:00Z"))
                .build();

        // When
        var outbox = mapper.toDomain(entity);

        // Then
        assertEquals("outbox-1", outbox.getId());
        assertEquals(IngestionDispatchStatus.FAILED, outbox.getStatus());
        assertEquals("bad json", outbox.getFailureReason());
        assertEquals(Instant.parse("2026-01-02T00:00:00Z"), outbox.getChronology().getUpdatedAt());
    }
}

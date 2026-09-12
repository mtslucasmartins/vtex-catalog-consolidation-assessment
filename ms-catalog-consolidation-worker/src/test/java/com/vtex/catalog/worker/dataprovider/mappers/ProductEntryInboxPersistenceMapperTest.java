package com.vtex.catalog.worker.dataprovider.mappers;

import com.vtex.catalog.worker.application.domain.processing.ProductEntryIdentifiers;
import com.vtex.catalog.worker.application.domain.processing.ProductEntryInbox;
import com.vtex.catalog.worker.application.domain.processing.ProductEntryStatus;
import com.vtex.catalog.worker.dataprovider.table.ProductEntryInboxTable;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProductEntryInboxPersistenceMapperTest {

    private final ProductEntryInboxPersistenceMapper mapper = new ProductEntryInboxPersistenceMapper();

    @Test
    void givenPendingInbox_whenToPendingEntity_thenSetsTimestamps() {
        // Given
        var inbox = ProductEntryInbox.pending(
                ProductEntryIdentifiers.from("ing-1:0", "ing-1"),
                "megastore#samsung#phone");

        // When
        var entity = mapper.toPendingEntity(inbox);

        // Then
        assertEquals("ing-1:0", entity.getCorrelationId());
        assertNull(entity.getStatus());
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
    }

    @Test
    void givenEntity_whenToDomain_thenMapsStatusAndTimestamps() {
        // Given
        var entity = ProductEntryInboxTable.builder()
                .correlationId("ing-1:0")
                .ingestionId("ing-1")
                .idempotencyKey("megastore#samsung#phone")
                .status(ProductEntryStatus.CREATED.name())
                .reason("Consolidated as CREATED")
                .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2026-01-02T00:00:00Z"))
                .build();

        // When
        var domain = mapper.toDomain(entity);

        // Then
        assertEquals(ProductEntryStatus.CREATED, domain.getStatus());
        assertEquals("Consolidated as CREATED", domain.getReason());
        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), domain.getCreatedAt());
    }
}

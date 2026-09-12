package com.vtex.catalog.ingester.entrypoint.api.mapper;

import com.vtex.catalog.ingester.application.domain.ingestion.IngestionDispatchStatus;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionId;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionSnapshot;
import com.vtex.catalog.ingester.entrypoint.api.dto.IngestionIn;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IngestionRestMapperTest {

    private final IngestionRestMapper mapper = new IngestionRestMapper();

    @Test
    void givenIngestionIn_whenToCommand_thenMapsFileMetadata() {
        // Given
        var in = IngestionIn.builder()
                .filename("products.json")
                .contentLength(12L)
                .content(new ByteArrayInputStream("[]".getBytes()))
                .build();

        // When
        var command = mapper.toCommand(in);

        // Then
        assertEquals("products.json", command.getFileName());
        assertEquals(12L, command.getContentLength());
    }

    @Test
    void givenSnapshot_whenToOut_thenMapsResponseFields() {
        // Given
        var created = Instant.parse("2026-01-01T00:00:00Z");
        var updated = Instant.parse("2026-01-02T00:00:00Z");
        var snapshot = IngestionSnapshot.builder()
                .id(IngestionId.of("ing-1"))
                .fileName("products.json")
                .status(IngestionDispatchStatus.PENDING)
                .failureReason(null)
                .createdAt(created)
                .updatedAt(updated)
                .build();

        // When
        var out = mapper.toOut(snapshot);

        // Then
        assertEquals("ing-1", out.getId());
        assertEquals("products.json", out.getFileName());
        assertEquals("PENDING", out.getStatus());
        assertEquals(created, out.getCreatedAt());
    }
}

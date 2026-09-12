package com.vtex.catalog.ingester.dataprovider.table;

import com.vtex.catalog.ingester.application.domain.ingestion.IngestionId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductIngestOutboxTableTest {

    @Test
    void givenIngestionId_whenPending_thenCreatesPendingRow() {
        // Given
        var ingestionId = IngestionId.of("ing-1");

        // When
        var row = ProductIngestOutboxTable.pending(ingestionId);

        // Then
        assertEquals(ProductIngestOutboxTable.PENDING, row.getStatus());
        assertEquals("ing-1", row.getIngestionId());
        assertEquals(0, row.getAttempts());
    }

    @Test
    void givenRetryableFailure_whenRegisterFailure_thenIncrementsAttempts() {
        // Given
        var row = ProductIngestOutboxTable.pending(IngestionId.of("ing-1"));

        // When
        row.registerFailure("timeout", 3);

        // Then
        assertEquals(1, row.getAttempts());
        assertEquals("timeout", row.getFailureReason());
        assertEquals(ProductIngestOutboxTable.PENDING, row.getStatus());
        assertFalse(row.isFailed());
    }

    @Test
    void givenMaxAttemptsReached_whenRegisterFailure_thenMarksFailed() {
        // Given
        var row = ProductIngestOutboxTable.pending(IngestionId.of("ing-1"));
        row.registerFailure("first", 2);
        row.registerFailure("second", 2);

        // When / Then
        assertTrue(row.isFailed());
        assertEquals(ProductIngestOutboxTable.FAILED, row.getStatus());
    }

    @Test
    void givenPendingRow_whenMarkDispatched_thenUpdatesStatus() {
        // Given
        var row = ProductIngestOutboxTable.pending(IngestionId.of("ing-1"));

        // When
        row.markDispatched();

        // Then
        assertEquals(ProductIngestOutboxTable.DISPATCHED, row.getStatus());
    }

    @Test
    void givenPendingRow_whenMarkFailed_thenUpdatesStatusAndReason() {
        // Given
        var row = ProductIngestOutboxTable.pending(IngestionId.of("ing-1"));

        // When
        row.markFailed("bad json");

        // Then
        assertEquals(ProductIngestOutboxTable.FAILED, row.getStatus());
        assertEquals("bad json", row.getFailureReason());
    }
}

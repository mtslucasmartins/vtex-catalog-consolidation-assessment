package com.vtex.catalog.ingester.application.domain.ingestion;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductIngestOutboxTest {

    @Test
    void givenIngestionId_whenPending_thenCreatesPendingOutbox() {
        // Given
        var ingestionId = IngestionId.of("ing-1");

        // When
        var outbox = ProductIngestOutbox.pending(ingestionId);

        // Then
        assertEquals(IngestionDispatchStatus.PENDING, outbox.getStatus());
        assertEquals(ingestionId, outbox.getIngestionId());
        assertEquals(0, outbox.getAttempts());
    }

    @Test
    void givenRetryableFailure_whenRegisterFailure_thenIncrementsAttempts() {
        // Given
        var outbox = ProductIngestOutbox.pending(IngestionId.of("ing-1"));

        // When
        outbox.registerFailure("timeout", 3);

        // Then
        assertEquals(1, outbox.getAttempts());
        assertEquals("timeout", outbox.getFailureReason());
        assertEquals(IngestionDispatchStatus.PENDING, outbox.getStatus());
        assertFalse(outbox.isFailed());
    }

    @Test
    void givenMaxAttemptsReached_whenRegisterFailure_thenMarksFailed() {
        // Given
        var outbox = ProductIngestOutbox.pending(IngestionId.of("ing-1"));
        outbox.registerFailure("first", 2);
        outbox.registerFailure("second", 2);

        // When / Then
        assertTrue(outbox.isFailed());
        assertEquals(IngestionDispatchStatus.FAILED, outbox.getStatus());
    }

    @Test
    void givenPendingOutbox_whenMarkDispatched_thenUpdatesStatus() {
        // Given
        var outbox = ProductIngestOutbox.pending(IngestionId.of("ing-1"));

        // When
        outbox.markDispatched();

        // Then
        assertEquals(IngestionDispatchStatus.DISPATCHED, outbox.getStatus());
    }

    @Test
    void givenPendingOutbox_whenMarkFailed_thenUpdatesStatusAndReason() {
        // Given
        var outbox = ProductIngestOutbox.pending(IngestionId.of("ing-1"));

        // When
        outbox.markFailed("bad json");

        // Then
        assertEquals(IngestionDispatchStatus.FAILED, outbox.getStatus());
        assertEquals("bad json", outbox.getFailureReason());
    }

    @Test
    void givenPendingOutbox_whenClaimForProcessing_thenUpdatesStatus() {
        // Given
        var outbox = ProductIngestOutbox.pending(IngestionId.of("ing-1"));

        // When
        outbox.claimForProcessing();

        // Then
        assertEquals(IngestionDispatchStatus.PROCESSING, outbox.getStatus());
        assertTrue(outbox.isDispatchable());
    }
}

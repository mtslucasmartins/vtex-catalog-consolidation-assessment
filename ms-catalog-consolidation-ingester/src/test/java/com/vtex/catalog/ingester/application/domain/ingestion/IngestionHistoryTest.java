package com.vtex.catalog.ingester.application.domain.ingestion;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IngestionHistoryTest {

    @Test
    void givenRecordedHistory_whenTouch_thenUpdatesUpdatedAt() throws InterruptedException {
        // Given
        var history = IngestionHistory.record(IngestionId.of("ing-1"), "products.json");
        var originalUpdatedAt = history.getUpdatedAt();
        Thread.sleep(2);

        // When
        history.touch();

        // Then
        assertTrue(history.getUpdatedAt().isAfter(originalUpdatedAt)
                || !history.getUpdatedAt().equals(originalUpdatedAt));
    }

    @Test
    void givenRecordedHistory_whenGetCreatedAt_thenReturnsChronologyCreatedAt() {
        // Given
        var history = IngestionHistory.record(IngestionId.of("ing-1"), "products.json");

        // When / Then
        assertNotEquals(null, history.getCreatedAt());
    }
}

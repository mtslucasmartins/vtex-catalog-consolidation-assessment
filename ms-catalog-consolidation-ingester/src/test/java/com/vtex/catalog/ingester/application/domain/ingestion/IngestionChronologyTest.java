package com.vtex.catalog.ingester.application.domain.ingestion;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IngestionChronologyTest {

    @Test
    void givenChronology_whenNow_thenSetsCreatedAndUpdatedAt() {
        // When
        var chronology = IngestionChronology.now();

        // Then
        assertNotNull(chronology.getCreatedAt());
        assertNotNull(chronology.getUpdatedAt());
    }

    @Test
    void givenChronology_whenTouch_thenUpdatesUpdatedAt() throws InterruptedException {
        // Given
        var chronology = IngestionChronology.now();
        var originalUpdatedAt = chronology.getUpdatedAt();
        Thread.sleep(2);

        // When
        chronology.touch();

        // Then
        assertTrue(chronology.getUpdatedAt().compareTo(originalUpdatedAt) >= 0);
    }
}

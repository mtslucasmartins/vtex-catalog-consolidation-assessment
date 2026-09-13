package com.vtex.catalog.worker.application.domain.processing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductEntryInboxTest {

    @Test
    void givenPendingInbox_whenIsCompleted_thenReturnsFalse() {
        // Given
        var inbox = ProductEntryInbox.pending(
                ProductEntryIdentifiers.from("ing-1:0", "ing-1"),
                "megastore#samsung#phone");

        // When / Then
        assertFalse(inbox.isCompleted());
    }

    @Test
    void givenCompletedInbox_whenIsCompleted_thenReturnsTrue() {
        // Given
        var inbox = ProductEntryInbox.completed(
                ProductEntryIdentifiers.from("ing-1:0", "ing-1"),
                "megastore#samsung#phone",
                ProductEntryStatus.LINKED,
                "Consolidated as LINKED");

        // When / Then
        assertTrue(inbox.isCompleted());
    }

    @Test
    void givenPendingInbox_whenComplete_thenSetsStatusReasonAndUpdatedAt() {
        // Given
        var inbox = ProductEntryInbox.pending(
                ProductEntryIdentifiers.from("ing-1:0", "ing-1"),
                "megastore#samsung#phone");

        // When
        inbox.complete(ProductEntryStatus.CREATED, "Consolidated as CREATED");

        // Then
        assertEquals(ProductEntryStatus.CREATED, inbox.getStatus());
        assertEquals("Consolidated as CREATED", inbox.getReason());
        assertNotNull(inbox.getUpdatedAt());
    }
}

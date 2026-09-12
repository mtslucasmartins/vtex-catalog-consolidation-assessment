package com.vtex.catalog.ingester.dataprovider.gateway.product;

import com.vtex.catalog.ingester.application.domain.ingestion.IngestionDispatchStatus;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionId;
import com.vtex.catalog.ingester.dataprovider.repository.ProductIngestOutboxRepository;
import com.vtex.catalog.ingester.dataprovider.table.ProductIngestOutboxTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductIngestOutboxGatewayImplTest {

    @Mock
    private ProductIngestOutboxRepository repository;

    @InjectMocks
    private ProductIngestOutboxGatewayImpl gateway;

    @Test
    void givenIngestionId_whenEnqueue_thenSavesPendingRow() {
        // Given
        var ingestionId = IngestionId.of("ing-1");
        when(repository.save(any(ProductIngestOutboxTable.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        gateway.enqueue(ingestionId);

        // Then
        var captor = ArgumentCaptor.forClass(ProductIngestOutboxTable.class);
        verify(repository).save(captor.capture());
        assertEquals(ProductIngestOutboxTable.PENDING, captor.getValue().getStatus());
    }

    @Test
    void givenPendingRow_whenIsPending_thenReturnsTrue() {
        // Given
        var ingestionId = IngestionId.of("ing-1");
        when(repository.findByIngestionId("ing-1")).thenReturn(Optional.of(pendingRow()));

        // When / Then
        assertTrue(gateway.isPending(ingestionId));
        assertEquals(IngestionDispatchStatus.PENDING, gateway.findStatusByIngestionId(ingestionId).orElseThrow());
    }

    @Test
    void givenExistingRow_whenMarkDispatched_thenUpdatesRow() {
        // Given
        var ingestionId = IngestionId.of("ing-1");
        var row = pendingRow();
        when(repository.findByIngestionId("ing-1")).thenReturn(Optional.of(row));
        when(repository.save(row)).thenReturn(row);

        // When
        gateway.markDispatched(ingestionId);

        // Then
        assertEquals(ProductIngestOutboxTable.DISPATCHED, row.getStatus());
    }

    @Test
    void givenExistingRow_whenMarkFailed_thenUpdatesRow() {
        // Given
        var ingestionId = IngestionId.of("ing-1");
        var row = pendingRow();
        when(repository.findByIngestionId("ing-1")).thenReturn(Optional.of(row));
        when(repository.save(row)).thenReturn(row);

        // When
        gateway.markFailed(ingestionId, "bad json");

        // Then
        assertEquals(ProductIngestOutboxTable.FAILED, row.getStatus());
        assertEquals("bad json", row.getFailureReason());
    }

    @Test
    void givenFailedRow_whenFindFailureReasonByIngestionId_thenReturnsReason() {
        // Given
        var row = pendingRow();
        row.markFailed("bad json");
        when(repository.findByIngestionId("ing-1")).thenReturn(Optional.of(row));

        // When
        var reason = gateway.findFailureReasonByIngestionId(IngestionId.of("ing-1"));

        // Then
        assertEquals(Optional.of("bad json"), reason);
    }

    @Test
    void givenPendingRows_whenClaimPending_thenMarksProcessingAndReturnsIngestionIds() {
        // Given
        var row = pendingRow();
        when(repository.findPendingForUpdate(5)).thenReturn(List.of(row));
        when(repository.saveAll(List.of(row))).thenReturn(List.of(row));

        // When
        var claimed = gateway.claimPending(5);

        // Then
        assertEquals(List.of(IngestionId.of("ing-1")), claimed);
        assertEquals(ProductIngestOutboxTable.PROCESSING, row.getStatus());
    }

    @Test
    void givenProcessingRow_whenRegisterFailureBelowMaxAttempts_thenResetsToPending() {
        // Given
        var row = pendingRow();
        row.markProcessing();
        when(repository.findByIngestionId("ing-1")).thenReturn(Optional.of(row));
        when(repository.save(row)).thenReturn(row);

        // When
        gateway.registerFailure(IngestionId.of("ing-1"), "kafka down", 5);

        // Then
        assertEquals(1, row.getAttempts());
        assertEquals(ProductIngestOutboxTable.PENDING, row.getStatus());
    }

    @Test
    void givenProcessingRow_whenRegisterFailureAtMaxAttempts_thenMarksFailed() {
        // Given
        var row = pendingRow();
        row.markProcessing();
        when(repository.findByIngestionId("ing-1")).thenReturn(Optional.of(row));
        when(repository.save(row)).thenReturn(row);

        // When
        gateway.registerFailure(IngestionId.of("ing-1"), "kafka down", 1);

        // Then
        assertEquals(ProductIngestOutboxTable.FAILED, row.getStatus());
    }

    @Test
    void givenMissingRow_whenMarkFailed_thenThrowsIllegalStateException() {
        // Given
        when(repository.findByIngestionId("ing-1")).thenReturn(Optional.empty());

        // When / Then
        assertThrows(IllegalStateException.class, () -> gateway.markFailed(IngestionId.of("ing-1"), "reason"));
    }

    private static ProductIngestOutboxTable pendingRow() {
        return ProductIngestOutboxTable.builder()
                .id("outbox-1")
                .ingestionId("ing-1")
                .status(ProductIngestOutboxTable.PENDING)
                .attempts(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}

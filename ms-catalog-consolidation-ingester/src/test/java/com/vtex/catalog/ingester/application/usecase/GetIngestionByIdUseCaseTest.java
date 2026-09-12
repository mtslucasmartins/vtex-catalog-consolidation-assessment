package com.vtex.catalog.ingester.application.usecase;

import com.vtex.catalog.ingester.application.common.exception.IngestionNotFoundException;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionDispatchStatus;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionHistory;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionId;
import com.vtex.catalog.ingester.application.gateway.history.IngestionHistoryGateway;
import com.vtex.catalog.ingester.application.gateway.product.ProductIngestOutboxGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetIngestionByIdUseCaseTest {

    @Mock
    private IngestionHistoryGateway historyGateway;

    @Mock
    private ProductIngestOutboxGateway outboxGateway;

    @InjectMocks
    private GetIngestionByIdUseCase useCase;

    @Test
    void givenHistoryAndOutbox_whenExecute_thenReturnsSnapshot() {
        // Given
        var ingestionId = IngestionId.of("ing-1");
        var history = IngestionHistory.record(ingestionId, "products.json");
        when(historyGateway.findById(ingestionId)).thenReturn(Optional.of(history));
        when(outboxGateway.findStatusByIngestionId(ingestionId))
                .thenReturn(Optional.of(IngestionDispatchStatus.DISPATCHED));
        when(outboxGateway.findFailureReasonByIngestionId(ingestionId)).thenReturn(Optional.empty());

        // When
        var snapshot = useCase.execute(GetIngestionByIdQuery.from(ingestionId));

        // Then
        assertEquals("products.json", snapshot.getFileName());
        assertEquals(IngestionDispatchStatus.DISPATCHED, snapshot.getStatus());
    }

    @Test
    void givenMissingHistory_whenExecute_thenThrowsIngestionNotFoundException() {
        // Given
        var ingestionId = IngestionId.of("ing-1");
        when(historyGateway.findById(ingestionId)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(IngestionNotFoundException.class,
                () -> useCase.execute(GetIngestionByIdQuery.from(ingestionId)));
    }

    @Test
    void givenMissingOutbox_whenExecute_thenThrowsIngestionNotFoundException() {
        // Given
        var ingestionId = IngestionId.of("ing-1");
        when(historyGateway.findById(ingestionId))
                .thenReturn(Optional.of(IngestionHistory.record(ingestionId, "products.json")));
        when(outboxGateway.findStatusByIngestionId(ingestionId)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(IngestionNotFoundException.class,
                () -> useCase.execute(GetIngestionByIdQuery.from(ingestionId)));
    }
}

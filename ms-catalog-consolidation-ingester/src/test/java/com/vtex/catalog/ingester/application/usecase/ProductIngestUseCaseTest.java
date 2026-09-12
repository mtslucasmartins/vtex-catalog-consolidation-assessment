package com.vtex.catalog.ingester.application.usecase;

import com.vtex.catalog.ingester.application.common.exception.IngestionNotFoundException;
import com.vtex.catalog.ingester.application.common.exception.InvalidIngestionFileException;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionDispatchStatus;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionHistory;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionId;
import com.vtex.catalog.ingester.application.domain.processing.ProductEntryPayload;
import com.vtex.catalog.ingester.application.gateway.history.IngestionHistoryGateway;
import com.vtex.catalog.ingester.application.gateway.product.ProductEntryEventGateway;
import com.vtex.catalog.ingester.application.gateway.product.ProductEntryReaderGateway;
import com.vtex.catalog.ingester.application.gateway.product.ProductIngestOutboxGateway;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductIngestUseCaseTest {

    @Mock
    private IngestionHistoryGateway historyGateway;

    @Mock
    private ProductIngestOutboxGateway outboxGateway;

    @Mock
    private ProductEntryReaderGateway readerGateway;

    @Mock
    private ProductEntryEventGateway eventGateway;

    private ProductIngestUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ProductIngestUseCase(
                historyGateway,
                outboxGateway,
                readerGateway,
                eventGateway,
                new SimpleMeterRegistry());
    }

    @Test
    void givenPendingOutbox_whenExecute_thenPublishesEventsAndMarksDispatched() {
        // Given
        var ingestionId = IngestionId.of("ing-1");
        var history = IngestionHistory.record(ingestionId, "products.json");
        var product = ProductEntryPayload.builder()
                .sellerProductId("a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d")
                .sellerName("MegaStore")
                .productName("Phone")
                .productBrand("Samsung")
                .productCategory("Electronics")
                .build();
        when(outboxGateway.isDispatchable(ingestionId)).thenReturn(true);
        when(historyGateway.findById(ingestionId)).thenReturn(Optional.of(history));
        when(readerGateway.read(anyString(), any())).thenAnswer(invocation -> {
            BiConsumer<Integer, ProductEntryPayload> consumer = invocation.getArgument(1);
            consumer.accept(0, product);
            return 1L;
        });

        // When
        useCase.execute(ProductIngestCommand.from(ingestionId));

        // Then
        verify(eventGateway).publish(ingestionId, 0, product);
        verify(outboxGateway).markDispatched(ingestionId);
        verify(historyGateway).save(any(IngestionHistory.class));
    }

    @Test
    void givenNonPendingOutbox_whenExecute_thenSkipsDispatch() {
        // Given
        var ingestionId = IngestionId.of("ing-1");
        when(outboxGateway.isDispatchable(ingestionId)).thenReturn(false);
        when(outboxGateway.findStatusByIngestionId(ingestionId))
                .thenReturn(Optional.of(IngestionDispatchStatus.DISPATCHED));

        // When
        useCase.execute(ProductIngestCommand.from(ingestionId));

        // Then
        verify(readerGateway, never()).read(anyString(), any());
        verify(outboxGateway, never()).markDispatched(any());
    }

    @Test
    void givenInvalidFile_whenExecute_thenMarksOutboxFailed() {
        // Given
        var ingestionId = IngestionId.of("ing-1");
        var history = IngestionHistory.record(ingestionId, "products.json");
        when(outboxGateway.isDispatchable(ingestionId)).thenReturn(true);
        when(historyGateway.findById(ingestionId)).thenReturn(Optional.of(history));
        when(readerGateway.read(anyString(), any()))
                .thenThrow(new InvalidIngestionFileException("bad json"));

        // When
        useCase.execute(ProductIngestCommand.from(ingestionId));

        // Then
        verify(outboxGateway).markFailed(ingestionId, "bad json");
        verify(outboxGateway, never()).markDispatched(any());
    }

    @Test
    void givenMissingHistory_whenExecute_thenThrowsIngestionNotFoundException() {
        // Given
        var ingestionId = IngestionId.of("ing-1");
        when(outboxGateway.isDispatchable(ingestionId)).thenReturn(true);
        when(historyGateway.findById(ingestionId)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(IngestionNotFoundException.class, () -> useCase.execute(ProductIngestCommand.from(ingestionId)));
        verify(readerGateway, never()).read(anyString(), any());
    }
}

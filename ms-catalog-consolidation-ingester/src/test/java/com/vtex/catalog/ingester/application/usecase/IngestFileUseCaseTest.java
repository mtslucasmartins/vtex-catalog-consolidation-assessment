package com.vtex.catalog.ingester.application.usecase;

import com.vtex.catalog.ingester.application.domain.ingestion.IngestionDispatchStatus;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionHistory;
import com.vtex.catalog.ingester.application.gateway.storage.FileStorageGateway;
import com.vtex.catalog.ingester.application.gateway.history.IngestionHistoryGateway;
import com.vtex.catalog.ingester.application.gateway.product.ProductIngestOutboxGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngestFileUseCaseTest {

    @Mock
    private FileStorageGateway storageGateway;

    @Mock
    private IngestionHistoryGateway historyGateway;

    @Mock
    private ProductIngestOutboxGateway outboxGateway;

    @InjectMocks
    private IngestFileUseCase useCase;

    @Test
    void givenValidFile_whenExecute_thenStoresHistoryAndEnqueuesOutbox() {
        // Given
        when(storageGateway.store(any(), eq("products.json"), eq(12L), any()))
                .thenReturn("ingestion-key");
        when(historyGateway.save(any(IngestionHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var command = IngestCommand.builder()
                .fileName("products.json")
                .contentLength(12L)
                .content(new ByteArrayInputStream("[]".getBytes()))
                .build();

        // When
        var snapshot = useCase.execute(command);

        // Then
        assertEquals("products.json", snapshot.getFileName());
        assertEquals(IngestionDispatchStatus.PENDING, snapshot.getStatus());
        verify(storageGateway).store(any(), eq("products.json"), eq(12L), any());
        verify(historyGateway).save(any(IngestionHistory.class));
        verify(outboxGateway).enqueue(any());
    }
}

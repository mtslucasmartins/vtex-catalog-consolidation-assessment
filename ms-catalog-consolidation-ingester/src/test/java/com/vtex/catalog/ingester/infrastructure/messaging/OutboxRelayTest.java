package com.vtex.catalog.ingester.infrastructure.messaging;

import com.vtex.catalog.ingester.application.domain.ingestion.IngestionDispatchStatus;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionId;
import com.vtex.catalog.ingester.application.gateway.product.ProductIngestOutboxGateway;
import com.vtex.catalog.ingester.application.usecase.ProductIngestUseCase;
import com.vtex.catalog.ingester.infrastructure.config.IngesterProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxRelayTest {

    @Mock
    private ProductIngestOutboxGateway outboxGateway;

    @Mock
    private ProductIngestUseCase productIngestUseCase;

    private OutboxRelay relay;

    @BeforeEach
    void setUp() {
        var properties = new IngesterProperties();
        properties.getOutbox().setIngestRelayBatchSize(10);
        properties.getOutbox().setMaxAttempts(5);
        relay = new OutboxRelay(outboxGateway, properties, productIngestUseCase, new SimpleMeterRegistry());
    }

    @Test
    void givenPendingOutboxRows_whenRelayIngestions_thenDispatchesEachIngestion() {
        // Given
        var ingestionId = IngestionId.of("ing-1");
        when(outboxGateway.claimPending(10)).thenReturn(List.of(ingestionId));

        // When
        relay.relayIngestions();

        // Then
        verify(productIngestUseCase).execute(any());
    }

    @Test
    void givenDispatchFailureAtMaxAttempts_whenRelayIngestions_thenRegistersFailure() {
        // Given
        var properties = new IngesterProperties();
        properties.getOutbox().setIngestRelayBatchSize(10);
        properties.getOutbox().setMaxAttempts(1);
        relay = new OutboxRelay(outboxGateway, properties, productIngestUseCase, new SimpleMeterRegistry());
        var ingestionId = IngestionId.of("ing-1");
        when(outboxGateway.claimPending(10)).thenReturn(List.of(ingestionId));
        doThrow(new RuntimeException("kafka down")).when(productIngestUseCase).execute(any());
        when(outboxGateway.findStatusByIngestionId(ingestionId))
                .thenReturn(Optional.of(IngestionDispatchStatus.FAILED));

        // When
        relay.relayIngestions();

        // Then
        verify(outboxGateway).registerFailure(eq(ingestionId), any(), eq(1));
    }

    @Test
    void givenDispatchFailure_whenRelayIngestions_thenRegistersFailureOnOutboxRow() {
        // Given
        var ingestionId = IngestionId.of("ing-1");
        when(outboxGateway.claimPending(10)).thenReturn(List.of(ingestionId));
        doThrow(new RuntimeException("kafka down")).when(productIngestUseCase).execute(any());
        when(outboxGateway.findStatusByIngestionId(ingestionId))
                .thenReturn(Optional.of(IngestionDispatchStatus.PENDING));

        // When
        relay.relayIngestions();

        // Then
        verify(outboxGateway).registerFailure(eq(ingestionId), any(), eq(5));
    }
}

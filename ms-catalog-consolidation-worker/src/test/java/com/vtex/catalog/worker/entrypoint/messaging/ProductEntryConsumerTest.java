package com.vtex.catalog.worker.entrypoint.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vtex.catalog.worker.application.domain.processing.ProductEntryStatus;
import com.vtex.catalog.worker.application.usecase.ProductEntryCommand;
import com.vtex.catalog.worker.application.usecase.ProductEntryUseCase;
import com.vtex.catalog.worker.infrastructure.messaging.EventSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductEntryConsumerTest {

    @Mock
    private ProductEntryUseCase productEntryUseCase;

    private ProductEntryConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new ProductEntryConsumer(productEntryUseCase, new EventSerializer(new ObjectMapper()));
    }

    @Test
    void givenValidKafkaPayload_whenConsume_thenDispatchesToUseCase() {
        // Given
        var payload = """
                {
                  "ingestionId": "ing-1",
                  "correlationId": "ing-1:0",
                  "product": {
                    "sellerProductId": "a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d",
                    "sellerName": "MegaStore",
                    "name": "Smartphone Galaxy S23",
                    "brand": "Samsung",
                    "category": "Phones"
                  }
                }
                """;
        when(productEntryUseCase.execute(any(ProductEntryCommand.class))).thenReturn(ProductEntryStatus.CREATED);

        // When
        consumer.consume(payload);

        // Then
        verify(productEntryUseCase).execute(any(ProductEntryCommand.class));
    }
}

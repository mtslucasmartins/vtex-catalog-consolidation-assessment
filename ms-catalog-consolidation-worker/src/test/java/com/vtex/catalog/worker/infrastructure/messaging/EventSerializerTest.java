package com.vtex.catalog.worker.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vtex.catalog.worker.application.domain.processing.ProductEntryPayload;
import com.vtex.catalog.worker.infrastructure.messaging.event.ProductEntryUpdateEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EventSerializerTest {

    private EventSerializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new EventSerializer(new ObjectMapper());
    }

    @Test
    void givenKafkaPayloadJson_whenDeserialize_thenMapsProductFields() {
        // Given
        var json = """
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

        // When
        var restored = serializer.deserialize(json, ProductEntryUpdateEvent.class);

        // Then
        assertEquals("ing-1", restored.getIngestionId());
        assertEquals("ing-1:0", restored.getCorrelationId());
        assertEquals("MegaStore", restored.getProduct().getSellerName());
    }

    @Test
    void givenProductEntryEvent_whenSerialize_thenProducesJsonPayload() {
        // Given
        var product = ProductEntryPayload.builder()
                .sellerProductId("a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d")
                .sellerName("MegaStore")
                .productName("Smartphone Galaxy S23")
                .productBrand("Samsung")
                .productCategory("Phones")
                .build();
        var event = ProductEntryUpdateEvent.builder()
                .ingestionId("ing-1")
                .correlationId("ing-1:0")
                .product(product)
                .build();

        // When
        var json = serializer.serialize(event);

        // Then
        assertEquals(true, json.contains("\"ingestionId\":\"ing-1\""));
        assertEquals(true, json.contains("\"sellerName\":\"MegaStore\""));
    }

    @Test
    void givenInvalidJson_whenDeserialize_thenThrowsIllegalArgumentException() {
        // Given
        var invalidJson = "{not-json";

        // When / Then
        assertThrows(IllegalArgumentException.class,
                () -> serializer.deserialize(invalidJson, ProductEntryUpdateEvent.class));
    }
}

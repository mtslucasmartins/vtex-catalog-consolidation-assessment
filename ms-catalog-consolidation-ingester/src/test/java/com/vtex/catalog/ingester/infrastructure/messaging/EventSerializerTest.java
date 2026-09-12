package com.vtex.catalog.ingester.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vtex.catalog.ingester.infrastructure.messaging.event.ProductEntryUpdateEvent;
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
                    "name": "Phone",
                    "brand": "Samsung",
                    "category": "Electronics"
                  }
                }
                """;

        // When
        var restored = serializer.deserialize(json, ProductEntryUpdateEvent.class);

        // Then
        assertEquals("ing-1:0", restored.getCorrelationId());
        assertEquals("MegaStore", restored.getProduct().getSellerName());
    }

    @Test
    void givenEvent_whenSerialize_thenProducesJson() {
        // Given
        var json = """
                {
                  "ingestionId": "ing-1",
                  "correlationId": "ing-1:0",
                  "product": {
                    "sellerProductId": "a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d",
                    "sellerName": "MegaStore",
                    "name": "Phone",
                    "brand": "Samsung",
                    "category": "Electronics"
                  }
                }
                """;
        var event = serializer.deserialize(json, ProductEntryUpdateEvent.class);

        // When
        var serialized = serializer.serialize(event);

        // Then
        assertEquals(true, serialized.contains("\"correlationId\":\"ing-1:0\""));
    }

    @Test
    void givenInvalidJson_whenDeserialize_thenThrowsIllegalArgumentException() {
        // When / Then
        assertThrows(IllegalArgumentException.class,
                () -> serializer.deserialize("{bad", ProductEntryUpdateEvent.class));
    }
}

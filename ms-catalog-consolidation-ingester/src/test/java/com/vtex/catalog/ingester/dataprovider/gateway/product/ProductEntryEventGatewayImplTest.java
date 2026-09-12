package com.vtex.catalog.ingester.dataprovider.gateway.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionId;
import com.vtex.catalog.ingester.application.domain.processing.ProductEntryPayload;
import com.vtex.catalog.ingester.infrastructure.config.IngesterProperties;
import com.vtex.catalog.ingester.infrastructure.messaging.EventSerializer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductEntryEventGatewayImplTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private ProductEntryEventGatewayImpl gateway;

    @BeforeEach
    void setUp() {
        var properties = new IngesterProperties();
        properties.getTopics().setProductEntryUpdate("catalog.product-entry.update");
        properties.getOutbox().setSendTimeoutMs(1000);
        gateway = new ProductEntryEventGatewayImpl(
                kafkaTemplate,
                new EventSerializer(new ObjectMapper()),
                properties,
                new SimpleMeterRegistry());
    }

    @Test
    void givenProductEntry_whenPublish_thenSendsKafkaMessageWithCorrelationId() {
        // Given
        var ingestionId = IngestionId.of("ing-1");
        var product = sampleProduct();
        var metadata = new RecordMetadata(new TopicPartition("catalog.product-entry.update", 0), 0, 0, 0, 0, 0);
        var sendResult = new SendResult<>(new ProducerRecord<>("catalog.product-entry.update", "ing-1:0", "{}"), metadata);
        when(kafkaTemplate.send(eq("catalog.product-entry.update"), eq("ing-1:0"), anyString()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        // When
        gateway.publish(ingestionId, 0, product);

        // Then
        verify(kafkaTemplate).send(eq("catalog.product-entry.update"), eq("ing-1:0"), anyString());
    }

    @Test
    void givenKafkaFailure_whenPublish_thenThrowsIllegalStateException() {
        // Given
        var ingestionId = IngestionId.of("ing-1");
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("broker down")));

        // When / Then
        assertThrows(IllegalStateException.class, () -> gateway.publish(ingestionId, 0, sampleProduct()));
    }

    private static ProductEntryPayload sampleProduct() {
        return ProductEntryPayload.builder()
                .sellerProductId("a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d")
                .sellerName("MegaStore")
                .productName("Phone")
                .productBrand("Samsung")
                .productCategory("Electronics")
                .build();
    }
}

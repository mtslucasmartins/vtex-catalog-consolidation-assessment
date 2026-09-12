package com.vtex.catalog.ingester.dataprovider.gateway.product;

import com.vtex.catalog.ingester.application.domain.ingestion.IngestionId;
import com.vtex.catalog.ingester.application.domain.processing.ProductEntryIdentifiers;
import com.vtex.catalog.ingester.application.domain.processing.ProductEntryPayload;
import com.vtex.catalog.ingester.application.gateway.product.ProductEntryEventGateway;
import com.vtex.catalog.ingester.infrastructure.config.IngesterProperties;
import com.vtex.catalog.ingester.infrastructure.messaging.EventSerializer;
import com.vtex.catalog.ingester.infrastructure.messaging.event.ProductEntryUpdateEvent;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.vtex.catalog.ingester.application.common.metrics.MetricNames.PRODUCT_ENTRIES_DISPATCHED;

@Component
@RequiredArgsConstructor
public class ProductEntryEventGatewayImpl implements ProductEntryEventGateway {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final EventSerializer serializer;
    private final IngesterProperties properties;
    private final MeterRegistry meterRegistry;

    @Override
    public void publish(IngestionId ingestionId, int entryIndex, ProductEntryPayload product) {
        var correlationId = ingestionId.getValue() + ":" + entryIndex;
        var identifiers = ProductEntryIdentifiers.from(correlationId, ingestionId.getValue());
        var event = ProductEntryUpdateEvent.builder()
                .ingestionId(identifiers.getIngestionId())
                .correlationId(identifiers.getCorrelationId())
                .product(product)
                .build();
        try {
            kafkaTemplate.send(
                            properties.getTopics().getProductEntryUpdate(),
                            correlationId,
                            serializer.serialize(event))
                    .get(properties.getOutbox().getSendTimeoutMs(), TimeUnit.MILLISECONDS);
            meterRegistry.counter(PRODUCT_ENTRIES_DISPATCHED, "outcome", "successful").increment();
        } catch (Exception e) {
            meterRegistry.counter(PRODUCT_ENTRIES_DISPATCHED, "outcome", "failed").increment();
            throw new IllegalStateException("Could not dispatch product entry " + correlationId, e);
        }
    }
}

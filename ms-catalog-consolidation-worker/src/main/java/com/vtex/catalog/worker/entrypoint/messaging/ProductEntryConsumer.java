package com.vtex.catalog.worker.entrypoint.messaging;

import com.vtex.catalog.worker.application.domain.processing.ProductEntryIdentifiers;
import com.vtex.catalog.worker.application.usecase.ProductEntryCommand;
import com.vtex.catalog.worker.application.usecase.ProductEntryUseCase;
import com.vtex.catalog.worker.infrastructure.messaging.EventSerializer;
import com.vtex.catalog.worker.infrastructure.messaging.event.ProductEntryUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProductEntryConsumer {

    private final ProductEntryUseCase productEntryUseCase;

    private final EventSerializer serializer;

    @KafkaListener(
            topics = "${worker.topics.product-entry-update}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String payload) {
        // deserializes the event and dispatches for processing.
        var event = this.serializer.deserialize(payload, ProductEntryUpdateEvent.class);
        var identifiers = ProductEntryIdentifiers.from(event.getCorrelationId(), event.getIngestionId());

        var status = this.productEntryUseCase.execute(ProductEntryCommand.from(identifiers, event.getProduct()));
        log.info("Product entry {} from ingestion {} processed: {}",
                event.getCorrelationId(), event.getIngestionId(), status);
    }
}

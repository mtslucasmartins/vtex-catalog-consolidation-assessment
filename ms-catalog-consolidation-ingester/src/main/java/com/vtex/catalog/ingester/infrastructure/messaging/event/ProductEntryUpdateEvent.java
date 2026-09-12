package com.vtex.catalog.ingester.infrastructure.messaging.event;

import com.vtex.catalog.ingester.application.domain.processing.ProductEntryPayload;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

/** One product entry to be processed by the worker. */
@Getter
@Builder
@Jacksonized
public class ProductEntryUpdateEvent {

    private final String ingestionId;
    private final String correlationId;
    private final ProductEntryPayload product;
}

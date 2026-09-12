package com.vtex.catalog.worker.infrastructure.messaging.event;

import com.vtex.catalog.worker.application.domain.processing.ProductEntryPayload;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

/** Published for every product entry in a file; consumed by the worker. */
@Getter
@Builder
@Jacksonized
public class ProductEntryUpdateEvent {

    private final String ingestionId;

    private final String correlationId;

    private final ProductEntryPayload product;
}

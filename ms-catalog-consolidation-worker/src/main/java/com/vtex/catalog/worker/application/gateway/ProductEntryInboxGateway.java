package com.vtex.catalog.worker.application.gateway;

import com.vtex.catalog.worker.application.domain.processing.ProductEntryInbox;
import com.vtex.catalog.worker.application.domain.processing.ProductEntryStatus;

import java.util.Optional;

public interface ProductEntryInboxGateway {

    Optional<ProductEntryInbox> findByCorrelationId(String correlationId);

    Optional<ProductEntryInbox> findByIdempotencyKey(String idempotencyKey);

    ProductEntryInbox claim(ProductEntryInbox inbox);

    ProductEntryInbox complete(String correlationId, ProductEntryStatus status, String reason);
}

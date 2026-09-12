package com.vtex.catalog.ingester.application.common.exception;

import com.vtex.catalog.ingester.application.domain.ingestion.IngestionId;

public class IngestionNotFoundException extends RuntimeException {

    public IngestionNotFoundException(IngestionId id) {
        super("Ingestion not found: " + id);
    }
}

package com.vtex.catalog.ingester.application.gateway.storage;

import com.vtex.catalog.ingester.application.domain.ingestion.IngestionId;
import com.vtex.catalog.ingester.application.common.stereotypes.Gateway;

import java.io.InputStream;

public interface FileStorageGateway extends Gateway {

    /** Persists the uploaded content and returns the object key the initializer will read from. */
    String store(IngestionId ingestionId, String fileName, long contentLength, InputStream content);

    InputStream open(String objectKey);
}

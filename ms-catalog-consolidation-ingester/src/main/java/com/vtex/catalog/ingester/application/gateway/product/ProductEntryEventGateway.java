package com.vtex.catalog.ingester.application.gateway.product;

import com.vtex.catalog.ingester.application.common.stereotypes.Gateway;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionId;
import com.vtex.catalog.ingester.application.domain.processing.ProductEntryPayload;

public interface ProductEntryEventGateway extends Gateway {

    void publish(IngestionId ingestionId, int entryIndex, ProductEntryPayload product);
}

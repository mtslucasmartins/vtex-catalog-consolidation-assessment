package com.vtex.catalog.ingester.application.gateway.product;

import com.vtex.catalog.ingester.application.domain.processing.ProductEntryPayload;

import java.util.function.BiConsumer;

public interface ProductEntryReaderGateway {

    long read(String filePath, BiConsumer<Integer, ProductEntryPayload> entryConsumer);
}

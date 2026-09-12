package com.vtex.catalog.worker.application.usecase.iterator;

import com.vtex.catalog.worker.application.gateway.CatalogGateway;
import com.vtex.catalog.worker.application.usecase.iterator.handlers.ProductEntryLinkHandler;
import com.vtex.catalog.worker.application.usecase.iterator.handlers.ProductEntryUpsertHandler;
import com.vtex.catalog.worker.application.usecase.iterator.handlers.ProductEntryValidationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductEntryChainFactory {

    private final CatalogGateway catalogGateway;

    public ProductEntryHandler getChain() {
        var validationHandler = new ProductEntryValidationHandler(catalogGateway);
        var upsertHandler = new ProductEntryUpsertHandler(catalogGateway);
        var linkHandler = new ProductEntryLinkHandler(catalogGateway);

        validationHandler.setNext(upsertHandler);
        upsertHandler.setNext(linkHandler);

        return validationHandler;
    }
}

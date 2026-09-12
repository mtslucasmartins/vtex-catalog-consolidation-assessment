package com.vtex.catalog.worker.application.usecase.iterator.handlers;

import com.vtex.catalog.worker.application.gateway.CatalogGateway;
import com.vtex.catalog.worker.application.usecase.iterator.AbstractProductEntryHandler;
import com.vtex.catalog.worker.application.usecase.iterator.ProductEntryContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProductEntryUpsertHandler extends AbstractProductEntryHandler {

    private final CatalogGateway catalogGateway;

    @Override
    public void handle(ProductEntryContext context) {
        var product = this.catalogGateway.findOrCreateProduct(context.getPayload());

        context.setExistingCatalogProduct(product.existing());
        context.setCatalogProductId(product.productId());

        next(context);
    }
}

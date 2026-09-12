package com.vtex.catalog.worker.application.usecase.iterator.handlers;

import com.vtex.catalog.worker.application.domain.processing.ProductEntryStatus;
import com.vtex.catalog.worker.application.gateway.CatalogGateway;
import com.vtex.catalog.worker.application.usecase.iterator.AbstractProductEntryHandler;
import com.vtex.catalog.worker.application.usecase.iterator.ProductEntryContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProductEntryLinkHandler extends AbstractProductEntryHandler {

    private final CatalogGateway catalogGateway;

    @Override
    public void handle(ProductEntryContext context) {
        var product = context.getPayload();

        catalogGateway.linkSellerProduct(
                product.getSellerName(),
                product.getSellerProductId(),
                context.getCatalogProductId());

        context.setStatus(context.isExistingCatalogProduct()
                ? ProductEntryStatus.LINKED
                : ProductEntryStatus.CREATED);
    }
}

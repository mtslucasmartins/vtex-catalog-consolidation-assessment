package com.vtex.catalog.worker.application.usecase.iterator.handlers;

import com.vtex.catalog.worker.application.common.exception.DomainException;
import com.vtex.catalog.worker.application.common.exception.InvalidProductException;
import com.vtex.catalog.worker.application.common.helpers.StringHelper;
import com.vtex.catalog.worker.application.domain.processing.ProductEntryStatus;
import com.vtex.catalog.worker.application.gateway.CatalogGateway;
import com.vtex.catalog.worker.application.usecase.iterator.AbstractProductEntryHandler;
import com.vtex.catalog.worker.application.usecase.iterator.ProductEntryContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProductEntryValidationHandler extends AbstractProductEntryHandler {

    private final CatalogGateway catalogGateway;

    @Override
    public void handle(ProductEntryContext context) {
        var product = context.getPayload();

        if (product == null) {
            throw new DomainException("Product entry is required.");
        }

        if (!StringHelper.isUuid(product.getSellerProductId())) {
            throw new InvalidProductException("Seller product id must be a valid UUID.");
        }

        if (this.catalogGateway.existsSellerLink(product.getSellerName(), product.getSellerProductId())) {
            context.setStatus(ProductEntryStatus.ALREADY_LINKED);
            return;
        }

        next(context);
    }
}

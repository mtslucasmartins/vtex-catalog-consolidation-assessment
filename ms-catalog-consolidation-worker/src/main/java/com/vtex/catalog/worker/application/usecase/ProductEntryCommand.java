package com.vtex.catalog.worker.application.usecase;

import com.vtex.catalog.worker.application.common.stereotypes.Intent;
import com.vtex.catalog.worker.application.domain.processing.ProductEntryPayload;
import com.vtex.catalog.worker.application.domain.processing.ProductEntryIdentifiers;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
@AllArgsConstructor
public final class ProductEntryCommand implements Intent {

    private ProductEntryIdentifiers identifiers;

    private ProductEntryPayload product;

    public static ProductEntryCommand from(ProductEntryIdentifiers identifiers, ProductEntryPayload product) {
        return ProductEntryCommand.builder().identifiers(identifiers).product(product).build();
    }
}

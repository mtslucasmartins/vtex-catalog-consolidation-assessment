package com.vtex.catalog.worker.application.usecase.iterator;

import com.vtex.catalog.worker.application.domain.processing.ProductEntryPayload;
import com.vtex.catalog.worker.application.domain.processing.ProductEntryStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor(staticName = "of")
public class ProductEntryContext {

    private final ProductEntryPayload payload;

    private ProductEntryStatus status;

    private Long catalogProductId;

    private boolean existingCatalogProduct;
}

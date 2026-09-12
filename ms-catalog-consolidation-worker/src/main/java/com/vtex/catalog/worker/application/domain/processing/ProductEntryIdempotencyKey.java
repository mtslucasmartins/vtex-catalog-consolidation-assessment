package com.vtex.catalog.worker.application.domain.processing;

import com.vtex.catalog.worker.application.common.helpers.StringHelper;

public final class ProductEntryIdempotencyKey {

    private ProductEntryIdempotencyKey() {
    }

    public static String from(ProductEntryPayload product) {
        return StringHelper.canonical(product.getSellerName())
                + "#"
                + StringHelper.sku(product.getProductBrand(), product.getProductName());
    }
}

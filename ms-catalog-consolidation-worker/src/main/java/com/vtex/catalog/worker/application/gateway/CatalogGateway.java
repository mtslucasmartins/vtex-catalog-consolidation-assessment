package com.vtex.catalog.worker.application.gateway;

import com.vtex.catalog.worker.application.common.stereotypes.Gateway;
import com.vtex.catalog.worker.application.domain.catalog.ProductMatch;
import com.vtex.catalog.worker.application.domain.processing.ProductEntryPayload;

public interface CatalogGateway extends Gateway {

    boolean existsSellerLink(String sellerName, String sellerProductId);

    ProductMatch findOrCreateProduct(ProductEntryPayload product);

    void linkSellerProduct(String sellerName, String sellerProductId, long productId);
}

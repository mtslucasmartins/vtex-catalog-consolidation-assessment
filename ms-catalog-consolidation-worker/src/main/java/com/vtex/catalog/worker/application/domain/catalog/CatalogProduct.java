package com.vtex.catalog.worker.application.domain.catalog;

import com.vtex.catalog.worker.application.common.stereotypes.DomainEntity;
import com.vtex.catalog.worker.application.domain.processing.ProductEntryPayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CatalogProduct implements DomainEntity {

    private Long id;

    private String sku;

    private String name;

    private String brand;

    private String category;

    public static CatalogProduct fromPayload(ProductEntryPayload payload, String sku) {
        return CatalogProduct.builder()
                .sku(sku)
                .name(payload.getProductName())
                .brand(payload.getProductBrand())
                .category(payload.getProductCategory())
                .build();
    }
}

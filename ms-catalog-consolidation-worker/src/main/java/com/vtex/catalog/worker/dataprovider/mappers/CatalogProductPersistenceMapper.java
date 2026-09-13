package com.vtex.catalog.worker.dataprovider.mappers;

import com.vtex.catalog.worker.application.domain.catalog.CatalogProduct;
import com.vtex.catalog.worker.dataprovider.table.ProductTable;
import org.springframework.stereotype.Component;

@Component
public class CatalogProductPersistenceMapper {

    public ProductTable toEntity(CatalogProduct product) {
        return ProductTable.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .brand(product.getBrand())
                .category(product.getCategory())
                .build();
    }

    public CatalogProduct toDomain(ProductTable entity) {
        return CatalogProduct.builder()
                .id(entity.getId())
                .sku(entity.getSku())
                .name(entity.getName())
                .brand(entity.getBrand())
                .category(entity.getCategory())
                .build();
    }
}

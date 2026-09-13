package com.vtex.catalog.worker.dataprovider.mappers;

import com.vtex.catalog.worker.application.domain.catalog.SellerProductLink;
import com.vtex.catalog.worker.dataprovider.table.SellerProductTable;
import org.springframework.stereotype.Component;

@Component
public class SellerProductLinkPersistenceMapper {

    public SellerProductTable toEntity(SellerProductLink link) {
        return SellerProductTable.builder()
                .id(link.getId())
                .sellerName(link.getSellerName())
                .sellerProductId(link.getSellerProductId())
                .productId(link.getProductId())
                .build();
    }

    public SellerProductLink toDomain(SellerProductTable entity) {
        return SellerProductLink.builder()
                .id(entity.getId())
                .sellerName(entity.getSellerName())
                .sellerProductId(entity.getSellerProductId())
                .productId(entity.getProductId())
                .build();
    }
}

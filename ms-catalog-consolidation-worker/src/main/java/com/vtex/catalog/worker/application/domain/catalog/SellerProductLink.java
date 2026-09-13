package com.vtex.catalog.worker.application.domain.catalog;

import com.vtex.catalog.worker.application.common.stereotypes.DomainEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SellerProductLink implements DomainEntity {

    private Long id;

    private String sellerName;

    private String sellerProductId;

    private long productId;

    public static SellerProductLink create(String sellerName, String sellerProductId, long productId) {
        return SellerProductLink.builder()
                .sellerName(sellerName)
                .sellerProductId(sellerProductId)
                .productId(productId)
                .build();
    }
}

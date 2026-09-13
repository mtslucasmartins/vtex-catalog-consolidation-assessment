package com.vtex.catalog.worker.dataprovider.mappers;

import com.vtex.catalog.worker.application.domain.catalog.SellerProductLink;
import com.vtex.catalog.worker.dataprovider.table.SellerProductTable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SellerProductLinkPersistenceMapperTest {

    private final SellerProductLinkPersistenceMapper mapper = new SellerProductLinkPersistenceMapper();

    @Test
    void givenLink_whenToEntity_thenMapsFields() {
        // Given
        var link = SellerProductLink.create("MegaStore", "prod-1", 30L);

        // When
        var entity = mapper.toEntity(link);

        // Then
        assertEquals("MegaStore", entity.getSellerName());
        assertEquals("prod-1", entity.getSellerProductId());
        assertEquals(30L, entity.getProductId());
    }

    @Test
    void givenEntity_whenToDomain_thenMapsFields() {
        // Given
        var entity = SellerProductTable.builder()
                .id(1L)
                .sellerName("MegaStore")
                .sellerProductId("prod-1")
                .productId(30L)
                .build();

        // When
        var link = mapper.toDomain(entity);

        // Then
        assertEquals("MegaStore", link.getSellerName());
        assertEquals(30L, link.getProductId());
    }
}

package com.vtex.catalog.worker.dataprovider.mappers;

import com.vtex.catalog.worker.application.domain.catalog.CatalogProduct;
import com.vtex.catalog.worker.dataprovider.table.ProductTable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CatalogProductPersistenceMapperTest {

    private final CatalogProductPersistenceMapper mapper = new CatalogProductPersistenceMapper();

    @Test
    void givenProduct_whenToEntity_thenMapsFields() {
        // Given
        var product = CatalogProduct.builder()
                .id(10L)
                .sku("samsung#phone")
                .name("Phone")
                .brand("Samsung")
                .category("Phones")
                .build();

        // When
        var entity = mapper.toEntity(product);

        // Then
        assertEquals(10L, entity.getId());
        assertEquals("samsung#phone", entity.getSku());
    }

    @Test
    void givenEntity_whenToDomain_thenMapsFields() {
        // Given
        var entity = ProductTable.builder()
                .id(10L)
                .sku("samsung#phone")
                .name("Phone")
                .brand("Samsung")
                .category("Phones")
                .build();

        // When
        var product = mapper.toDomain(entity);

        // Then
        assertEquals(10L, product.getId());
        assertEquals("samsung#phone", product.getSku());
    }
}

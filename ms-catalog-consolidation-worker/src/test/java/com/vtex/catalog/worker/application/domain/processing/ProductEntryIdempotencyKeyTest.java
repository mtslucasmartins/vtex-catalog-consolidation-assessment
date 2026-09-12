package com.vtex.catalog.worker.application.domain.processing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductEntryIdempotencyKeyTest {

    @Test
    void givenProductPayload_whenFrom_thenBuildsSellerAndSkuKey() {
        // Given
        var product = ProductEntryPayload.builder()
                .sellerProductId("a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d")
                .sellerName("MegaStore")
                .productName("Smartphone Galaxy S23")
                .productBrand("Samsung")
                .productCategory("Phones")
                .build();

        // When
        var key = ProductEntryIdempotencyKey.from(product);

        // Then
        assertEquals("megastore#samsung#smartphone-galaxy-s23", key);
    }
}

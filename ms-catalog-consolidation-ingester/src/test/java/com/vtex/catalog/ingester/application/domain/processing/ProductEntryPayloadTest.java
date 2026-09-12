package com.vtex.catalog.ingester.application.domain.processing;

import com.vtex.catalog.ingester.application.common.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductEntryPayloadTest {

    @Test
    void givenValidFields_whenBuild_thenNormalizesValues() {
        // When
        var payload = ProductEntryPayload.builder()
                .sellerProductId("a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d")
                .sellerName("  MegaStore ")
                .productName(" Smartphone Galaxy S23 ")
                .productBrand(" Samsung ")
                .productCategory(" Phones ")
                .build();

        // Then
        assertEquals("MegaStore", payload.getSellerName());
        assertEquals("Smartphone Galaxy S23", payload.getProductName());
    }

    @Test
    void givenMissingSellerName_whenBuild_thenThrowsDomainException() {
        // When / Then
        assertThrows(DomainException.class, () -> ProductEntryPayload.builder()
                .sellerProductId("a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d")
                .productName("Phone")
                .productBrand("Samsung")
                .build());
    }
}

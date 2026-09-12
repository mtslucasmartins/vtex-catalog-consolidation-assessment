package com.vtex.catalog.worker.application.domain.processing;

import com.vtex.catalog.worker.application.common.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductEntryPayloadTest {

    @Test
    void givenValidFields_whenBuild_thenNormalizesValues() {
        // Given / When
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
        assertEquals("Samsung", payload.getProductBrand());
    }

    @Test
    void givenMissingSellerProductId_whenBuild_thenThrowsDomainException() {
        // When / Then
        assertThrows(DomainException.class, () -> ProductEntryPayload.builder()
                .sellerName("MegaStore")
                .productName("Phone")
                .productBrand("Samsung")
                .build());
    }

    @Test
    void givenSymbolOnlyProductName_whenBuild_thenThrowsDomainException() {
        // When / Then
        assertThrows(DomainException.class, () -> ProductEntryPayload.builder()
                .sellerProductId("a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d")
                .sellerName("MegaStore")
                .productName("***")
                .productBrand("Samsung")
                .build());
    }
}

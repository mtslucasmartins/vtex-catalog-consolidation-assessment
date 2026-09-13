package com.vtex.catalog.worker.application.domain.catalog;

import com.vtex.catalog.worker.application.domain.processing.ProductEntryPayload;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CatalogProductTest {

    @Test
    void givenPayloadAndSku_whenFromPayload_thenMapsFields() {
        // Given
        var payload = ProductEntryPayload.builder()
                .sellerProductId("seller-prod-1")
                .sellerName("MegaStore")
                .productName("Smartphone Galaxy S23")
                .productBrand("Samsung")
                .productCategory("Phones")
                .build();

        // When
        var product = CatalogProduct.fromPayload(payload, "samsung#smartphone-galaxy-s23");

        // Then
        assertEquals("samsung#smartphone-galaxy-s23", product.getSku());
        assertEquals("Smartphone Galaxy S23", product.getName());
        assertEquals("Samsung", product.getBrand());
        assertEquals("Phones", product.getCategory());
    }
}

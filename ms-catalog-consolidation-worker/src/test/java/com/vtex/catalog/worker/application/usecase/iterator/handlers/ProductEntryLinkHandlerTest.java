package com.vtex.catalog.worker.application.usecase.iterator.handlers;

import com.vtex.catalog.worker.application.domain.processing.ProductEntryPayload;
import com.vtex.catalog.worker.application.domain.processing.ProductEntryStatus;
import com.vtex.catalog.worker.application.gateway.CatalogGateway;
import com.vtex.catalog.worker.application.usecase.iterator.ProductEntryContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductEntryLinkHandlerTest {

    @Mock
    private CatalogGateway catalogGateway;

    @Test
    void givenExistingCatalogProduct_whenHandle_thenLinksAndMarksLinked() {
        // Given
        var handler = new ProductEntryLinkHandler(catalogGateway);
        var product = sampleProduct();
        var context = ProductEntryContext.of(product);
        context.setCatalogProductId(99L);
        context.setExistingCatalogProduct(true);

        // When
        handler.handle(context);

        // Then
        verify(catalogGateway).linkSellerProduct("MegaStore", product.getSellerProductId(), 99L);
        assertEquals(ProductEntryStatus.LINKED, context.getStatus());
    }

    @Test
    void givenNewCatalogProduct_whenHandle_thenLinksAndMarksCreated() {
        // Given
        var handler = new ProductEntryLinkHandler(catalogGateway);
        var product = sampleProduct();
        var context = ProductEntryContext.of(product);
        context.setCatalogProductId(11L);
        context.setExistingCatalogProduct(false);

        // When
        handler.handle(context);

        // Then
        verify(catalogGateway).linkSellerProduct("MegaStore", product.getSellerProductId(), 11L);
        assertEquals(ProductEntryStatus.CREATED, context.getStatus());
    }

    private static ProductEntryPayload sampleProduct() {
        return ProductEntryPayload.builder()
                .sellerProductId("a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d")
                .sellerName("MegaStore")
                .productName("Smartphone Galaxy S23")
                .productBrand("Samsung")
                .productCategory("Phones")
                .build();
    }
}

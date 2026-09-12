package com.vtex.catalog.worker.application.usecase.iterator.handlers;

import com.vtex.catalog.worker.application.domain.catalog.ProductMatch;
import com.vtex.catalog.worker.application.domain.processing.ProductEntryPayload;
import com.vtex.catalog.worker.application.gateway.CatalogGateway;
import com.vtex.catalog.worker.application.usecase.iterator.ProductEntryContext;
import com.vtex.catalog.worker.application.usecase.iterator.ProductEntryHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductEntryUpsertHandlerTest {

    @Mock
    private CatalogGateway catalogGateway;

    @Mock
    private ProductEntryHandler nextHandler;

    @Test
    void givenExistingCatalogProduct_whenHandle_thenSetsContextAndDelegatesToNext() {
        // Given
        var handler = new ProductEntryUpsertHandler(catalogGateway);
        handler.setNext(nextHandler);
        var product = sampleProduct();
        var context = ProductEntryContext.of(product);
        when(catalogGateway.findOrCreateProduct(product)).thenReturn(new ProductMatch(42L, true));

        // When
        handler.handle(context);

        // Then
        assertEquals(42L, context.getCatalogProductId());
        assertTrue(context.isExistingCatalogProduct());
        verify(nextHandler).handle(context);
    }

    @Test
    void givenNewCatalogProduct_whenHandle_thenSetsContextAndDelegatesToNext() {
        // Given
        var handler = new ProductEntryUpsertHandler(catalogGateway);
        handler.setNext(nextHandler);
        var product = sampleProduct();
        var context = ProductEntryContext.of(product);
        when(catalogGateway.findOrCreateProduct(product)).thenReturn(new ProductMatch(7L, false));

        // When
        handler.handle(context);

        // Then
        assertEquals(7L, context.getCatalogProductId());
        assertFalse(context.isExistingCatalogProduct());
        verify(nextHandler).handle(context);
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

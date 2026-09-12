package com.vtex.catalog.worker.application.usecase.iterator;

import com.vtex.catalog.worker.application.domain.catalog.ProductMatch;
import com.vtex.catalog.worker.application.domain.processing.ProductEntryPayload;
import com.vtex.catalog.worker.application.domain.processing.ProductEntryStatus;
import com.vtex.catalog.worker.application.gateway.CatalogGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductEntryChainExecutorTest {

    @Mock
    private CatalogGateway catalogGateway;

    @Test
    void givenNewProduct_whenExecute_thenReturnsCreated() {
        // Given
        var factory = new ProductEntryChainFactory(catalogGateway);
        var executor = new ProductEntryChainExecutor(factory);
        var product = sampleProduct();
        when(catalogGateway.existsSellerLink(anyString(), anyString())).thenReturn(false);
        when(catalogGateway.findOrCreateProduct(product)).thenReturn(new ProductMatch(5L, false));

        // When
        var status = executor.execute(product);

        // Then
        assertEquals(ProductEntryStatus.CREATED, status);
        verify(catalogGateway).linkSellerProduct("MegaStore", product.getSellerProductId(), 5L);
    }

    @Test
    void givenExistingCatalogProduct_whenExecute_thenReturnsLinked() {
        // Given
        var factory = new ProductEntryChainFactory(catalogGateway);
        var executor = new ProductEntryChainExecutor(factory);
        var product = sampleProduct();
        when(catalogGateway.existsSellerLink(anyString(), anyString())).thenReturn(false);
        when(catalogGateway.findOrCreateProduct(product)).thenReturn(new ProductMatch(8L, true));

        // When
        var status = executor.execute(product);

        // Then
        assertEquals(ProductEntryStatus.LINKED, status);
        verify(catalogGateway).linkSellerProduct("MegaStore", product.getSellerProductId(), 8L);
    }

    @Test
    void givenSellerAlreadyLinked_whenExecute_thenReturnsAlreadyLinked() {
        // Given
        var factory = new ProductEntryChainFactory(catalogGateway);
        var executor = new ProductEntryChainExecutor(factory);
        var product = sampleProduct();
        when(catalogGateway.existsSellerLink("MegaStore", product.getSellerProductId())).thenReturn(true);

        // When
        var status = executor.execute(product);

        // Then
        assertEquals(ProductEntryStatus.ALREADY_LINKED, status);
        verify(catalogGateway, never()).findOrCreateProduct(any());
        verify(catalogGateway, never()).linkSellerProduct(anyString(), anyString(), anyLong());
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

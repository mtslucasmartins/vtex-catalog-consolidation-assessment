package com.vtex.catalog.worker.application.usecase.iterator;

import com.vtex.catalog.worker.application.gateway.CatalogGateway;
import com.vtex.catalog.worker.application.usecase.iterator.handlers.ProductEntryValidationHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@ExtendWith(MockitoExtension.class)
class ProductEntryChainFactoryTest {

    @Mock
    private CatalogGateway catalogGateway;

    @Test
    void givenCatalogGateway_whenGetChain_thenReturnsValidationHandlerAsHead() {
        // Given
        var factory = new ProductEntryChainFactory(catalogGateway);

        // When
        var chain = factory.getChain();

        // Then
        assertInstanceOf(ProductEntryValidationHandler.class, chain);
    }
}

package com.vtex.catalog.worker.application.usecase.iterator.handlers;

import com.vtex.catalog.worker.application.common.exception.InvalidProductException;
import com.vtex.catalog.worker.application.domain.processing.ProductEntryPayload;
import com.vtex.catalog.worker.application.domain.processing.ProductEntryStatus;
import com.vtex.catalog.worker.application.gateway.CatalogGateway;
import com.vtex.catalog.worker.application.usecase.iterator.ProductEntryContext;
import com.vtex.catalog.worker.application.usecase.iterator.ProductEntryHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductEntryValidationHandlerTest {

    private static final String VALID_SELLER_PRODUCT_ID = "a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d";

    @Mock
    private CatalogGateway catalogGateway;

    @Mock
    private ProductEntryHandler nextHandler;

    @Test
    void givenInvalidSellerProductId_whenHandle_thenThrowsInvalidProductException() {
        // Given
        var handler = new ProductEntryValidationHandler(catalogGateway);
        var context = ProductEntryContext.of(validProduct("not-a-uuid"));

        // When / Then
        var exception = assertThrows(InvalidProductException.class, () -> handler.handle(context));
        assertEquals("Seller product id must be a valid UUID.", exception.getMessage());
        verify(catalogGateway, never()).existsSellerLink(any(), any());
    }

    @Test
    void givenSevenCharUuidSegment_whenHandle_thenThrowsInvalidProductException() {
        // Given — accepted by lenient UUID.fromString, must be rejected at validation
        var handler = new ProductEntryValidationHandler(catalogGateway);
        var context = ProductEntryContext.of(validProduct("ddddeee-ffff-4000-1111-222233334444"));

        // When / Then
        assertThrows(InvalidProductException.class, () -> handler.handle(context));
        verify(catalogGateway, never()).existsSellerLink(any(), any());
    }

    @Test
    void givenValidUuidAndNoExistingLink_whenHandle_thenDelegatesToNext() {
        // Given
        var handler = new ProductEntryValidationHandler(catalogGateway);
        handler.setNext(nextHandler);
        when(catalogGateway.existsSellerLink("MegaStore", VALID_SELLER_PRODUCT_ID)).thenReturn(false);
        var context = ProductEntryContext.of(validProduct(VALID_SELLER_PRODUCT_ID));

        // When
        handler.handle(context);

        // Then
        verify(nextHandler).handle(context);
    }

    @Test
    void givenValidUuidAndExistingLink_whenHandle_thenMarksAlreadyLinked() {
        // Given
        var handler = new ProductEntryValidationHandler(catalogGateway);
        handler.setNext(nextHandler);
        when(catalogGateway.existsSellerLink("MegaStore", VALID_SELLER_PRODUCT_ID)).thenReturn(true);
        var context = ProductEntryContext.of(validProduct(VALID_SELLER_PRODUCT_ID));

        // When
        handler.handle(context);

        // Then
        assertEquals(ProductEntryStatus.ALREADY_LINKED, context.getStatus());
        verify(nextHandler, never()).handle(any());
    }

    private static ProductEntryPayload validProduct(String sellerProductId) {
        return ProductEntryPayload.builder()
                .sellerProductId(sellerProductId)
                .sellerName("MegaStore")
                .productName("Smartphone Galaxy S23")
                .productBrand("Samsung")
                .productCategory("Phones")
                .build();
    }
}

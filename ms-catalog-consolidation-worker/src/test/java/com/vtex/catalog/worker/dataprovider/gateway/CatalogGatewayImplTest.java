package com.vtex.catalog.worker.dataprovider.gateway;

import com.vtex.catalog.worker.application.domain.processing.ProductEntryPayload;
import com.vtex.catalog.worker.dataprovider.mappers.CatalogProductPersistenceMapper;
import com.vtex.catalog.worker.dataprovider.mappers.SellerProductLinkPersistenceMapper;
import com.vtex.catalog.worker.dataprovider.repository.ProductRepository;
import com.vtex.catalog.worker.dataprovider.repository.SellerProductRepository;
import com.vtex.catalog.worker.dataprovider.table.ProductTable;
import com.vtex.catalog.worker.dataprovider.table.SellerProductTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogGatewayImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SellerProductRepository sellerProductRepository;

    private CatalogGatewayImpl gateway;

    @BeforeEach
    void setUp() {
        gateway = new CatalogGatewayImpl(
                productRepository,
                sellerProductRepository,
                new CatalogProductPersistenceMapper(),
                new SellerProductLinkPersistenceMapper());
    }

    @Test
    void givenExistingSku_whenFindOrCreateProduct_thenReturnsExistingMatch() {
        // Given
        var product = sampleProduct();
        when(productRepository.findBySku("samsung#smartphone-galaxy-s23"))
                .thenReturn(Optional.of(ProductTable.builder().id(10L).sku("samsung#smartphone-galaxy-s23").build()));

        // When
        var match = gateway.findOrCreateProduct(product);

        // Then
        assertEquals(10L, match.productId());
        assertTrue(match.existing());
    }

    @Test
    void givenUnknownSku_whenFindOrCreateProduct_thenCreatesProduct() {
        // Given
        var product = sampleProduct();
        when(productRepository.findBySku("samsung#smartphone-galaxy-s23")).thenReturn(Optional.empty());
        when(productRepository.save(any(ProductTable.class)))
                .thenAnswer(invocation -> {
                    ProductTable saved = invocation.getArgument(0);
                    return ProductTable.builder()
                            .id(20L)
                            .sku(saved.getSku())
                            .name(saved.getName())
                            .brand(saved.getBrand())
                            .category(saved.getCategory())
                            .build();
                });

        // When
        var match = gateway.findOrCreateProduct(product);

        // Then
        assertEquals(20L, match.productId());
        assertFalse(match.existing());
        var captor = ArgumentCaptor.forClass(ProductTable.class);
        verify(productRepository).save(captor.capture());
        assertEquals("samsung#smartphone-galaxy-s23", captor.getValue().getSku());
    }

    @Test
    void givenConcurrentInsert_whenFindOrCreateProduct_thenRefetchesExisting() {
        // Given
        var product = sampleProduct();
        when(productRepository.findBySku("samsung#smartphone-galaxy-s23"))
                .thenReturn(Optional.empty(), Optional.of(ProductTable.builder()
                        .id(10L)
                        .sku("samsung#smartphone-galaxy-s23")
                        .build()));
        when(productRepository.save(any(ProductTable.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate sku"));

        // When
        var match = gateway.findOrCreateProduct(product);

        // Then
        assertEquals(10L, match.productId());
        assertTrue(match.existing());
    }

    @Test
    void givenSellerLinkRequest_whenLinkSellerProduct_thenPersistsLink() {
        // Given
        var product = sampleProduct();
        when(sellerProductRepository.save(any(SellerProductTable.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        gateway.linkSellerProduct(product.getSellerName(), product.getSellerProductId(), 30L);

        // Then
        var captor = ArgumentCaptor.forClass(SellerProductTable.class);
        verify(sellerProductRepository).save(captor.capture());
        assertEquals("MegaStore", captor.getValue().getSellerName());
        assertEquals(30L, captor.getValue().getProductId());
    }

    @Test
    void givenSellerAndProductId_whenExistsSellerLink_thenDelegatesToRepository() {
        // Given
        when(sellerProductRepository.existsLink("MegaStore", "a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d"))
                .thenReturn(true);

        // When
        var exists = gateway.existsSellerLink("MegaStore", "a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d");

        // Then
        assertTrue(exists);
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

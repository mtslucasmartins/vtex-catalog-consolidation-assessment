package com.vtex.catalog.worker.dataprovider.gateway;

import com.vtex.catalog.worker.application.common.helpers.StringHelper;
import com.vtex.catalog.worker.application.domain.catalog.CatalogProduct;
import com.vtex.catalog.worker.application.domain.catalog.ProductMatch;
import com.vtex.catalog.worker.application.domain.catalog.SellerProductLink;
import com.vtex.catalog.worker.application.domain.processing.ProductEntryPayload;
import com.vtex.catalog.worker.application.gateway.CatalogGateway;
import com.vtex.catalog.worker.dataprovider.mappers.CatalogProductPersistenceMapper;
import com.vtex.catalog.worker.dataprovider.mappers.SellerProductLinkPersistenceMapper;
import com.vtex.catalog.worker.dataprovider.repository.ProductRepository;
import com.vtex.catalog.worker.dataprovider.repository.SellerProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CatalogGatewayImpl implements CatalogGateway {

    private final ProductRepository productRepository;

    private final SellerProductRepository sellerProductRepository;

    private final CatalogProductPersistenceMapper productMapper;

    private final SellerProductLinkPersistenceMapper sellerProductLinkMapper;

    @Override
    public boolean existsSellerLink(String sellerName, String sellerProductId) {
        return sellerProductRepository.existsLink(sellerName, sellerProductId);
    }

    @Override
    public ProductMatch findOrCreateProduct(ProductEntryPayload product) {
        var sku = StringHelper.sku(product.getProductBrand(), product.getProductName());
        var existingProduct = productRepository.findBySku(sku);
        if (existingProduct.isPresent()) {
            return new ProductMatch(productMapper.toDomain(existingProduct.get()).getId(), true);
        }

        try {
            var saved = productRepository.save(
                    productMapper.toEntity(CatalogProduct.fromPayload(product, sku)));
            return new ProductMatch(saved.getId(), false);
        } catch (DataIntegrityViolationException exception) {
            return productRepository.findBySku(sku)
                    .map(row -> new ProductMatch(productMapper.toDomain(row).getId(), true))
                    .orElseThrow(() -> exception);
        }
    }

    @Override
    public void linkSellerProduct(String sellerName, String sellerProductId, long productId) {
        sellerProductRepository.save(
                sellerProductLinkMapper.toEntity(SellerProductLink.create(sellerName, sellerProductId, productId)));
    }
}

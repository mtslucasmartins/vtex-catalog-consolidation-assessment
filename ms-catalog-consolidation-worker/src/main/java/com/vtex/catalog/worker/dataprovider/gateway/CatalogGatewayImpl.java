package com.vtex.catalog.worker.dataprovider.gateway;

import com.vtex.catalog.worker.application.common.helpers.StringHelper;
import com.vtex.catalog.worker.application.domain.catalog.ProductMatch;
import com.vtex.catalog.worker.application.domain.processing.ProductEntryPayload;
import com.vtex.catalog.worker.application.gateway.CatalogGateway;
import com.vtex.catalog.worker.dataprovider.repository.ProductRepository;
import com.vtex.catalog.worker.dataprovider.repository.SellerProductRepository;
import com.vtex.catalog.worker.dataprovider.table.ProductTable;
import com.vtex.catalog.worker.dataprovider.table.SellerProductTable;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CatalogGatewayImpl implements CatalogGateway {

    private final ProductRepository productRepository;

    private final SellerProductRepository sellerProductRepository;

    @Override
    public boolean existsSellerLink(String sellerName, String sellerProductId) {
        return sellerProductRepository.existsLink(sellerName, sellerProductId);
    }

    @Override
    public ProductMatch findOrCreateProduct(ProductEntryPayload product) {
        var sku = StringHelper.sku(product.getProductBrand(), product.getProductName());
        var existingProduct = productRepository.findBySku(sku);
        if (existingProduct.isPresent()) {
            return new ProductMatch(existingProduct.get().getId(), true);
        }

        try {
            var saved = productRepository.save(ProductTable.builder()
                    .sku(sku)
                    .name(product.getProductName())
                    .brand(product.getProductBrand())
                    .category(product.getProductCategory())
                    .build());
            return new ProductMatch(saved.getId(), false);
        } catch (DataIntegrityViolationException exception) {
            return productRepository.findBySku(sku)
                    .map(row -> new ProductMatch(row.getId(), true))
                    .orElseThrow(() -> exception);
        }
    }

    @Override
    public void linkSellerProduct(String sellerName, String sellerProductId, long productId) {
        sellerProductRepository.save(SellerProductTable.builder()
                .sellerName(sellerName)
                .sellerProductId(sellerProductId)
                .productId(productId)
                .build());
    }
}

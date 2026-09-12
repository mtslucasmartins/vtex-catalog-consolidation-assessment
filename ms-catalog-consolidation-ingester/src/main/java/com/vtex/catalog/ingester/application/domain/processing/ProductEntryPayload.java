package com.vtex.catalog.ingester.application.domain.processing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vtex.catalog.ingester.application.common.helpers.StringHelper;
import com.vtex.catalog.ingester.application.common.stereotypes.ValueObject;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
public class ProductEntryPayload extends ValueObject {

    private String sellerProductId;

    private String sellerName;

    private String productName;

    private String productBrand;

    private String productCategory;

    @Builder
    @Jacksonized
    private ProductEntryPayload(
            @JsonProperty("sellerProductId") String sellerProductId,
            @JsonProperty("sellerName") String sellerName,
            @JsonProperty("name") String productName,
            @JsonProperty("brand") String productBrand,
            @JsonProperty("category") String productCategory) {
        this.setSellerProductId(sellerProductId);
        this.setSellerName(sellerName);
        this.setProductName(productName);
        this.setProductBrand(productBrand);
        this.setProductCategory(productCategory);
    }

    private void setSellerProductId(String sellerProductId) {
        assertNotEmpty(sellerProductId, "Seller's product id is required.");
        this.sellerProductId = StringHelper.normalize(sellerProductId);
    }

    private void setSellerName(String sellerName) {
        assertNotEmpty(sellerName, "Seller's name is required.");
        this.sellerName = StringHelper.normalize(sellerName);
    }

    private void setProductName(String productName) {
        assertNotEmpty(productName, "Product name is required.");
        assertContainsLetterOrNumber(productName, "Product name must contain letters or numbers.");
        this.productName = StringHelper.normalize(productName);
    }

    private void setProductBrand(String productBrand) {
        this.productBrand = productBrand == null ? null : StringHelper.normalize(productBrand);
    }

    private void setProductCategory(String productCategory) {
        this.productCategory = productCategory == null ? null : StringHelper.normalize(productCategory);
    }
}

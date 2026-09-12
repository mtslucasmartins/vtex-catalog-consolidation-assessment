package com.vtex.catalog.ingester.dataprovider.file;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vtex.catalog.ingester.application.domain.processing.ProductEntryPayload;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

/** Wire format of one element in a seller file (PascalCase keys as sellers send them). */
@Getter
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductEntryIn {

    @JsonProperty("Id")
    private final String id;

    @JsonProperty("Name")
    private final String name;

    @JsonProperty("Brand")
    private final String brand;

    @JsonProperty("Category")
    private final String category;

    @JsonProperty("SellerName")
    private final String sellerName;

    public ProductEntryPayload toDomain() {
        return ProductEntryPayload.builder()
                .sellerName(sellerName)
                .sellerProductId(id)
                .productName(name)
                .productBrand(brand)
                .productCategory(category)
                .build();
    }
}

package com.vtex.catalog.ingester.application.domain.processing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
@AllArgsConstructor
public class ProductEntryIdentifiers {

    private String correlationId;

    private String ingestionId;

    public static ProductEntryIdentifiers from(String correlationId, String ingestionId) {
        return ProductEntryIdentifiers.builder().correlationId(correlationId).ingestionId(ingestionId).build();
    }
}

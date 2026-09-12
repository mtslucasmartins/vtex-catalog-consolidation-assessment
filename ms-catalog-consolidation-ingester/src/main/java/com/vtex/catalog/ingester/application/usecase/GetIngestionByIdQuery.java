package com.vtex.catalog.ingester.application.usecase;

import com.vtex.catalog.ingester.application.common.stereotypes.Intent;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionId;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public final class GetIngestionByIdQuery implements Intent {

    private final IngestionId ingestionId;

    public static GetIngestionByIdQuery from(IngestionId ingestionId) {
        return GetIngestionByIdQuery.builder().ingestionId(ingestionId).build();
    }
}

package com.vtex.catalog.ingester.application.gateway.history;

import com.vtex.catalog.ingester.application.common.stereotypes.Gateway;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionHistory;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionId;

import java.util.Optional;

public interface IngestionHistoryGateway extends Gateway {

    IngestionHistory save(IngestionHistory history);

    Optional<IngestionHistory> findById(IngestionId id);
}

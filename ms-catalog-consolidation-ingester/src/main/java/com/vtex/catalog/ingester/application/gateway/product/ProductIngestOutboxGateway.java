package com.vtex.catalog.ingester.application.gateway.product;

import com.vtex.catalog.ingester.application.common.stereotypes.Gateway;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionDispatchStatus;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionId;

import java.util.List;
import java.util.Optional;

public interface ProductIngestOutboxGateway extends Gateway {

    void enqueue(IngestionId ingestionId);

    Optional<IngestionDispatchStatus> findStatusByIngestionId(IngestionId ingestionId);

    Optional<String> findFailureReasonByIngestionId(IngestionId ingestionId);

    boolean isPending(IngestionId ingestionId);

    boolean isDispatchable(IngestionId ingestionId);

    List<IngestionId> claimPending(int batchSize);

    void registerFailure(IngestionId ingestionId, String reason, int maxAttempts);

    void markDispatched(IngestionId ingestionId);

    void markFailed(IngestionId ingestionId, String reason);
}

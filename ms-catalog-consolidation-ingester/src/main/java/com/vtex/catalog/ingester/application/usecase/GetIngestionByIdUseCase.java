package com.vtex.catalog.ingester.application.usecase;

import com.vtex.catalog.ingester.application.common.exception.IngestionNotFoundException;
import com.vtex.catalog.ingester.application.common.stereotypes.UseCase;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionSnapshot;
import com.vtex.catalog.ingester.application.gateway.history.IngestionHistoryGateway;
import com.vtex.catalog.ingester.application.gateway.product.ProductIngestOutboxGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class GetIngestionByIdUseCase implements UseCase<GetIngestionByIdQuery, IngestionSnapshot> {

    private final IngestionHistoryGateway historyGateway;

    private final ProductIngestOutboxGateway outboxGateway;

    @Override
    @Transactional(readOnly = true)
    public IngestionSnapshot execute(GetIngestionByIdQuery query) {
        var ingestionId = query.getIngestionId();
        var history = historyGateway.findById(ingestionId)
                .orElseThrow(() -> new IngestionNotFoundException(ingestionId));

        var status = outboxGateway.findStatusByIngestionId(ingestionId)
                .orElseThrow(() -> new IngestionNotFoundException(ingestionId));
        var failureReason = outboxGateway.findFailureReasonByIngestionId(ingestionId).orElse(null);

        return IngestionSnapshot.builder()
                .id(history.getId())
                .fileName(history.getFileName())
                .status(status)
                .failureReason(failureReason)
                .createdAt(history.getCreatedAt())
                .updatedAt(history.getUpdatedAt())
                .build();
    }
}

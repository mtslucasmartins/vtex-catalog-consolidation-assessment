package com.vtex.catalog.ingester.application.usecase;

import com.vtex.catalog.ingester.application.common.stereotypes.UseCase;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionDispatchStatus;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionHistory;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionId;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionSnapshot;
import com.vtex.catalog.ingester.application.gateway.storage.FileStorageGateway;
import com.vtex.catalog.ingester.application.gateway.history.IngestionHistoryGateway;
import com.vtex.catalog.ingester.application.gateway.product.ProductIngestOutboxGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class IngestFileUseCase implements UseCase<IngestCommand, IngestionSnapshot> {

    private final FileStorageGateway storageGateway;

    private final IngestionHistoryGateway historyGateway;

    private final ProductIngestOutboxGateway outboxGateway;

    @Override
    @Transactional
    public IngestionSnapshot execute(IngestCommand command) {
        var id = IngestionId.generate();

        storageGateway.store(id, command.getFileName(), command.getContentLength(), command.getContent());

        var history = historyGateway.save(IngestionHistory.record(id, command.getFileName()));
        outboxGateway.enqueue(id);

        log.info("Ingestion {} received file '{}'", id, command.getFileName());

        return IngestionSnapshot.builder()
                .id(id)
                .fileName(history.getFileName())
                .status(IngestionDispatchStatus.PENDING)
                .createdAt(history.getCreatedAt())
                .updatedAt(history.getUpdatedAt())
                .build();
    }
}

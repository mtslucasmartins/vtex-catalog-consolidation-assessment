package com.vtex.catalog.ingester.dataprovider.gateway.product;

import com.vtex.catalog.ingester.application.domain.ingestion.IngestionDispatchStatus;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionId;
import com.vtex.catalog.ingester.application.gateway.product.ProductIngestOutboxGateway;
import com.vtex.catalog.ingester.dataprovider.repository.ProductIngestOutboxRepository;
import com.vtex.catalog.ingester.dataprovider.table.ProductIngestOutboxTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductIngestOutboxGatewayImpl implements ProductIngestOutboxGateway {

    private final ProductIngestOutboxRepository repository;

    @Override
    @Transactional
    public void enqueue(IngestionId ingestionId) {
        repository.save(ProductIngestOutboxTable.pending(ingestionId));
    }

    @Override
    public Optional<IngestionDispatchStatus> findStatusByIngestionId(IngestionId ingestionId) {
        return repository.findByIngestionId(ingestionId.getValue())
                .map(row -> IngestionDispatchStatus.valueOf(row.getStatus()));
    }

    @Override
    public Optional<String> findFailureReasonByIngestionId(IngestionId ingestionId) {
        return repository.findByIngestionId(ingestionId.getValue())
                .map(ProductIngestOutboxTable::getFailureReason);
    }

    @Override
    public boolean isPending(IngestionId ingestionId) {
        return findStatusByIngestionId(ingestionId)
                .map(IngestionDispatchStatus.PENDING::equals)
                .orElse(false);
    }

    @Override
    public boolean isDispatchable(IngestionId ingestionId) {
        return findStatusByIngestionId(ingestionId)
                .map(status -> status == IngestionDispatchStatus.PENDING
                        || status == IngestionDispatchStatus.PROCESSING)
                .orElse(false);
    }

    @Override
    @Transactional
    public List<IngestionId> claimPending(int batchSize) {
        var rows = repository.findPendingForUpdate(batchSize);
        if (rows.isEmpty()) {
            return List.of();
        }
        rows.forEach(ProductIngestOutboxTable::markProcessing);
        return repository.saveAll(rows).stream()
                .map(row -> IngestionId.of(row.getIngestionId()))
                .toList();
    }

    @Override
    @Transactional
    public void registerFailure(IngestionId ingestionId, String reason, int maxAttempts) {
        var row = requireRow(ingestionId);
        row.registerFailure(reason, maxAttempts);
        repository.save(row);
    }

    @Override
    @Transactional
    public void markDispatched(IngestionId ingestionId) {
        var row = requireRow(ingestionId);
        row.markDispatched();
        repository.save(row);
    }

    @Override
    @Transactional
    public void markFailed(IngestionId ingestionId, String reason) {
        var row = requireRow(ingestionId);
        row.markFailed(reason);
        repository.save(row);
    }

    private ProductIngestOutboxTable requireRow(IngestionId ingestionId) {
        return repository.findByIngestionId(ingestionId.getValue())
                .orElseThrow(() -> new IllegalStateException("Outbox row not found for ingestion " + ingestionId));
    }
}

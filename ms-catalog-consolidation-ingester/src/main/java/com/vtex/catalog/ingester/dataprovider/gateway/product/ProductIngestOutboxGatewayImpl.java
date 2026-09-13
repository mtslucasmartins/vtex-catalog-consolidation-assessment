package com.vtex.catalog.ingester.dataprovider.gateway.product;

import com.vtex.catalog.ingester.application.domain.ingestion.IngestionDispatchStatus;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionId;
import com.vtex.catalog.ingester.application.domain.ingestion.ProductIngestOutbox;
import com.vtex.catalog.ingester.application.gateway.product.ProductIngestOutboxGateway;
import com.vtex.catalog.ingester.dataprovider.mappers.ProductIngestOutboxPersistenceMapper;
import com.vtex.catalog.ingester.dataprovider.repository.ProductIngestOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductIngestOutboxGatewayImpl implements ProductIngestOutboxGateway {

    private final ProductIngestOutboxRepository repository;

    private final ProductIngestOutboxPersistenceMapper mapper;

    @Override
    @Transactional
    public void enqueue(IngestionId ingestionId) {
        repository.save(mapper.toEntity(ProductIngestOutbox.pending(ingestionId)));
    }

    @Override
    public Optional<IngestionDispatchStatus> findStatusByIngestionId(IngestionId ingestionId) {
        return findOutbox(ingestionId).map(ProductIngestOutbox::getStatus);
    }

    @Override
    public Optional<String> findFailureReasonByIngestionId(IngestionId ingestionId) {
        return findOutbox(ingestionId).map(ProductIngestOutbox::getFailureReason);
    }

    @Override
    public boolean isPending(IngestionId ingestionId) {
        return findStatusByIngestionId(ingestionId)
                .map(IngestionDispatchStatus.PENDING::equals)
                .orElse(false);
    }

    @Override
    public boolean isDispatchable(IngestionId ingestionId) {
        return findOutbox(ingestionId)
                .map(ProductIngestOutbox::isDispatchable)
                .orElse(false);
    }

    @Override
    @Transactional
    public List<IngestionId> claimPending(int batchSize) {
        var claimed = repository.findPendingForUpdate(batchSize).stream()
                .map(mapper::toDomain)
                .peek(ProductIngestOutbox::claimForProcessing)
                .map(mapper::toEntity)
                .toList();
        if (claimed.isEmpty()) {
            return List.of();
        }
        return repository.saveAll(claimed).stream()
                .map(row -> IngestionId.of(row.getIngestionId()))
                .toList();
    }

    @Override
    @Transactional
    public void registerFailure(IngestionId ingestionId, String reason, int maxAttempts) {
        var outbox = requireOutbox(ingestionId);
        outbox.registerFailure(reason, maxAttempts);
        repository.save(mapper.toEntity(outbox));
    }

    @Override
    @Transactional
    public void markDispatched(IngestionId ingestionId) {
        var outbox = requireOutbox(ingestionId);
        outbox.markDispatched();
        repository.save(mapper.toEntity(outbox));
    }

    @Override
    @Transactional
    public void markFailed(IngestionId ingestionId, String reason) {
        var outbox = requireOutbox(ingestionId);
        outbox.markFailed(reason);
        repository.save(mapper.toEntity(outbox));
    }

    private Optional<ProductIngestOutbox> findOutbox(IngestionId ingestionId) {
        return repository.findByIngestionId(ingestionId.getValue()).map(mapper::toDomain);
    }

    private ProductIngestOutbox requireOutbox(IngestionId ingestionId) {
        return findOutbox(ingestionId)
                .orElseThrow(() -> new IllegalStateException("Outbox row not found for ingestion " + ingestionId));
    }
}

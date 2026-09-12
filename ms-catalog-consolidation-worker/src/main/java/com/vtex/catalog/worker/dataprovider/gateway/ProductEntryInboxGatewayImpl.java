package com.vtex.catalog.worker.dataprovider.gateway;

import com.vtex.catalog.worker.application.domain.processing.ProductEntryInbox;
import com.vtex.catalog.worker.application.domain.processing.ProductEntryStatus;
import com.vtex.catalog.worker.application.gateway.ProductEntryInboxGateway;
import com.vtex.catalog.worker.dataprovider.mappers.ProductEntryInboxPersistenceMapper;
import com.vtex.catalog.worker.dataprovider.repository.ProductEntryInboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductEntryInboxGatewayImpl implements ProductEntryInboxGateway {

    private final ProductEntryInboxRepository repository;

    private final ProductEntryInboxPersistenceMapper mapper;

    @Override
    public Optional<ProductEntryInbox> findByCorrelationId(String correlationId) {
        return this.repository.findById(correlationId).map(mapper::toDomain);
    }

    @Override
    public Optional<ProductEntryInbox> findByIdempotencyKey(String idempotencyKey) {
        return this.repository.findByIdempotencyKey(idempotencyKey).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public ProductEntryInbox claim(ProductEntryInbox inbox) {
        try {
            return mapper.toDomain(repository.save(mapper.toPendingEntity(inbox)));
        } catch (DataIntegrityViolationException exception) {
            return findExisting(inbox).orElseThrow(() -> exception);
        }
    }

    @Override
    @Transactional
    public ProductEntryInbox complete(String correlationId, ProductEntryStatus status, String reason) {
        var entity = repository.findById(correlationId)
                .orElseThrow(() -> new IllegalStateException("Inbox row not found: " + correlationId));
        entity.setStatus(status.name());
        entity.setReason(reason);
        entity.setUpdatedAt(Instant.now());
        return mapper.toDomain(repository.save(entity));
    }

    private Optional<ProductEntryInbox> findExisting(ProductEntryInbox inbox) {
        return repository.findById(inbox.getDetails().getCorrelationId()).map(mapper::toDomain);
    }
}

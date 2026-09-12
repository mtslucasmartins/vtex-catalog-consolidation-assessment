package com.vtex.catalog.ingester.dataprovider.gateway.history;

import com.vtex.catalog.ingester.application.domain.ingestion.IngestionHistory;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionId;
import com.vtex.catalog.ingester.application.gateway.history.IngestionHistoryGateway;
import com.vtex.catalog.ingester.dataprovider.mappers.IngestionHistoryPersistenceMapper;
import com.vtex.catalog.ingester.dataprovider.repository.IngestionHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class IngestionHistoryGatewayImpl implements IngestionHistoryGateway {

    private final IngestionHistoryRepository repository;

    private final IngestionHistoryPersistenceMapper mapper;

    @Override
    public IngestionHistory save(IngestionHistory history) {
        return mapper.toDomain(repository.save(mapper.toEntity(history)));
    }

    @Override
    public Optional<IngestionHistory> findById(IngestionId id) {
        return repository.findById(id.getValue()).map(mapper::toDomain);
    }
}

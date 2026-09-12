package com.vtex.catalog.ingester.dataprovider.gateway.history;

import com.vtex.catalog.ingester.application.domain.ingestion.IngestionHistory;
import com.vtex.catalog.ingester.application.domain.ingestion.IngestionId;
import com.vtex.catalog.ingester.dataprovider.mappers.IngestionHistoryPersistenceMapper;
import com.vtex.catalog.ingester.dataprovider.repository.IngestionHistoryRepository;
import com.vtex.catalog.ingester.dataprovider.table.IngestionHistoryTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngestionHistoryGatewayImplTest {

    @Mock
    private IngestionHistoryRepository repository;

    private IngestionHistoryGatewayImpl gateway;

    @BeforeEach
    void setUp() {
        gateway = new IngestionHistoryGatewayImpl(repository, new IngestionHistoryPersistenceMapper());
    }

    @Test
    void givenHistory_whenSave_thenPersistsAndReturnsDomain() {
        // Given
        var history = IngestionHistory.record(IngestionId.of("ing-1"), "products.json");
        when(repository.save(any(IngestionHistoryTable.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        var saved = gateway.save(history);

        // Then
        assertEquals("products.json", saved.getFileName());
        verify(repository).save(any(IngestionHistoryTable.class));
    }

    @Test
    void givenExistingEntity_whenFindById_thenReturnsDomain() {
        // Given
        var now = Instant.parse("2026-01-01T00:00:00Z");
        when(repository.findById("ing-1")).thenReturn(Optional.of(IngestionHistoryTable.builder()
                .id("ing-1")
                .fileName("products.json")
                .createdAt(now)
                .updatedAt(now)
                .build()));

        // When
        var found = gateway.findById(IngestionId.of("ing-1"));

        // Then
        assertTrue(found.isPresent());
        assertEquals("products.json", found.get().getFileName());
    }
}

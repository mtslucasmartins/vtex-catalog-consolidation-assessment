package com.vtex.catalog.worker.dataprovider.gateway;

import com.vtex.catalog.worker.application.domain.processing.ProductEntryIdentifiers;
import com.vtex.catalog.worker.application.domain.processing.ProductEntryInbox;
import com.vtex.catalog.worker.application.domain.processing.ProductEntryStatus;
import com.vtex.catalog.worker.dataprovider.mappers.ProductEntryInboxPersistenceMapper;
import com.vtex.catalog.worker.dataprovider.repository.ProductEntryInboxRepository;
import com.vtex.catalog.worker.dataprovider.table.ProductEntryInboxTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductEntryInboxGatewayImplTest {

    private static final String CORRELATION_ID = "ing-1:0";

    private static final String INGESTION_ID = "ing-1";

    private static final String IDEMPOTENCY_KEY = "megastore#samsung#phone";

    @Mock
    private ProductEntryInboxRepository repository;

    private ProductEntryInboxGatewayImpl gateway;

    private ProductEntryInboxPersistenceMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProductEntryInboxPersistenceMapper();
        gateway = new ProductEntryInboxGatewayImpl(repository, mapper);
    }

    @Test
    void givenPendingInbox_whenClaim_thenPersistsPendingRow() {
        // Given
        var inbox = ProductEntryInbox.pending(
                ProductEntryIdentifiers.from(CORRELATION_ID, INGESTION_ID),
                IDEMPOTENCY_KEY);
        when(repository.save(any(ProductEntryInboxTable.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        var claimed = gateway.claim(inbox);

        // Then
        assertNull(claimed.getStatus());
        assertEquals(CORRELATION_ID, claimed.getDetails().getCorrelationId());
        verify(repository).save(any(ProductEntryInboxTable.class));
    }

    @Test
    void givenDuplicateCorrelation_whenClaim_thenReturnsExistingRow() {
        // Given
        var inbox = ProductEntryInbox.pending(
                ProductEntryIdentifiers.from(CORRELATION_ID, INGESTION_ID),
                IDEMPOTENCY_KEY);
        var existing = ProductEntryInboxTable.builder()
                .correlationId(CORRELATION_ID)
                .ingestionId(INGESTION_ID)
                .idempotencyKey(IDEMPOTENCY_KEY)
                .status(ProductEntryStatus.LINKED.name())
                .reason("done")
                .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2026-01-01T00:00:00Z"))
                .build();
        when(repository.save(any(ProductEntryInboxTable.class))).thenThrow(new DataIntegrityViolationException("dup"));
        when(repository.findById(CORRELATION_ID)).thenReturn(Optional.of(existing));

        // When
        var claimed = gateway.claim(inbox);

        // Then
        assertEquals(ProductEntryStatus.LINKED, claimed.getStatus());
    }

    @Test
    void givenClaimedRow_whenComplete_thenUpdatesStatusAndTimestamp() {
        // Given
        var entity = ProductEntryInboxTable.builder()
                .correlationId(CORRELATION_ID)
                .ingestionId(INGESTION_ID)
                .idempotencyKey(IDEMPOTENCY_KEY)
                .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2026-01-01T00:00:00Z"))
                .build();
        when(repository.findById(CORRELATION_ID)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        var completed = gateway.complete(CORRELATION_ID, ProductEntryStatus.CREATED, "Consolidated as CREATED");

        // Then
        assertEquals(ProductEntryStatus.CREATED, completed.getStatus());
        assertEquals("Consolidated as CREATED", completed.getReason());
    }
}

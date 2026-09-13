package com.vtex.catalog.worker.application.usecase;

import com.vtex.catalog.worker.application.common.exception.InvalidProductException;
import com.vtex.catalog.worker.application.common.exception.LockNotAcquiredException;
import com.vtex.catalog.worker.application.domain.processing.ProductEntryIdentifiers;
import com.vtex.catalog.worker.application.domain.processing.ProductEntryInbox;
import com.vtex.catalog.worker.application.domain.processing.ProductEntryPayload;
import com.vtex.catalog.worker.application.domain.processing.ProductEntryStatus;
import com.vtex.catalog.worker.application.gateway.DistributedLockGateway;
import com.vtex.catalog.worker.application.gateway.ProductEntryInboxGateway;
import com.vtex.catalog.worker.application.usecase.iterator.ProductEntryChainExecutor;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.locks.Lock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductEntryUseCaseTest {

    private static final String CORRELATION_ID = "corr-1";

    private static final String INGESTION_ID = "ing-1";

    private static final String IDEMPOTENCY_KEY = "megastore#samsung#smartphone-galaxy-s23";

    @Mock
    private ProductEntryChainExecutor chainExecutor;

    @Mock
    private ProductEntryInboxGateway inboxGateway;

    @Mock
    private DistributedLockGateway lockGateway;

    @Mock
    private Lock lock;

    private final MeterRegistry meterRegistry = new SimpleMeterRegistry();

    private ProductEntryUseCase useCase;

    private ProductEntryCommand command;

    private ProductEntryPayload product;

    @BeforeEach
    void setUp() {
        useCase = new ProductEntryUseCase(chainExecutor, inboxGateway, lockGateway, meterRegistry);
        product = ProductEntryPayload.builder()
                .sellerProductId("a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d")
                .sellerName("MegaStore")
                .productName("Smartphone Galaxy S23")
                .productBrand("Samsung")
                .productCategory("Phones")
                .build();
        command = ProductEntryCommand.from(
                ProductEntryIdentifiers.from(CORRELATION_ID, INGESTION_ID),
                product);
    }

    @Test
    void givenNoExistingInbox_whenExecute_thenClaimsProcessesAndCompletes() {
        // Given
        when(lockGateway.acquire(IDEMPOTENCY_KEY)).thenReturn(lock);
        when(inboxGateway.findByCorrelationId(CORRELATION_ID)).thenReturn(Optional.empty());
        when(inboxGateway.claim(any(ProductEntryInbox.class)))
                .thenReturn(ProductEntryInbox.pending(command.getIdentifiers(), IDEMPOTENCY_KEY));
        when(chainExecutor.execute(product)).thenReturn(ProductEntryStatus.LINKED);
        when(inboxGateway.complete(CORRELATION_ID, ProductEntryStatus.LINKED, "Consolidated as LINKED"))
                .thenReturn(ProductEntryInbox.completed(
                        command.getIdentifiers(),
                        IDEMPOTENCY_KEY,
                        ProductEntryStatus.LINKED,
                        "Consolidated as LINKED"));

        // When
        var status = useCase.execute(command);

        // Then
        assertEquals(ProductEntryStatus.LINKED, status);
        InOrder order = inOrder(inboxGateway, chainExecutor);
        order.verify(inboxGateway).claim(any(ProductEntryInbox.class));
        order.verify(chainExecutor).execute(product);
        order.verify(inboxGateway).complete(CORRELATION_ID, ProductEntryStatus.LINKED, "Consolidated as LINKED");
        verify(lock).unlock();
    }

    @Test
    void givenCompletedInbox_whenExecute_thenSkipsProcessing() {
        // Given
        when(lockGateway.acquire(IDEMPOTENCY_KEY)).thenReturn(lock);
        when(inboxGateway.findByCorrelationId(CORRELATION_ID))
                .thenReturn(Optional.of(ProductEntryInbox.completed(
                        command.getIdentifiers(),
                        IDEMPOTENCY_KEY,
                        ProductEntryStatus.ALREADY_LINKED,
                        "Consolidated as ALREADY_LINKED")));

        // When
        var status = useCase.execute(command);

        // Then
        assertEquals(ProductEntryStatus.ALREADY_LINKED, status);
        verify(inboxGateway, never()).claim(any());
        verify(chainExecutor, never()).execute(any());
        verify(inboxGateway, never()).complete(any(), any(), any());
        verify(lock).unlock();
    }

    @Test
    void givenDifferentCorrelation_whenExecute_thenProcessesAndCompletes() {
        // Given
        when(lockGateway.acquire(IDEMPOTENCY_KEY)).thenReturn(lock);
        when(inboxGateway.findByCorrelationId(CORRELATION_ID)).thenReturn(Optional.empty());
        when(inboxGateway.claim(any(ProductEntryInbox.class)))
                .thenReturn(ProductEntryInbox.pending(command.getIdentifiers(), IDEMPOTENCY_KEY));
        when(chainExecutor.execute(product)).thenReturn(ProductEntryStatus.ALREADY_LINKED);
        when(inboxGateway.complete(
                        CORRELATION_ID,
                        ProductEntryStatus.ALREADY_LINKED,
                        "Consolidated as ALREADY_LINKED"))
                .thenReturn(ProductEntryInbox.completed(
                        command.getIdentifiers(),
                        IDEMPOTENCY_KEY,
                        ProductEntryStatus.ALREADY_LINKED,
                        "Consolidated as ALREADY_LINKED"));

        // When
        var status = useCase.execute(command);

        // Then
        assertEquals(ProductEntryStatus.ALREADY_LINKED, status);
        verify(inboxGateway).claim(any(ProductEntryInbox.class));
        verify(chainExecutor).execute(product);
        verify(inboxGateway).complete(
                CORRELATION_ID, ProductEntryStatus.ALREADY_LINKED, "Consolidated as ALREADY_LINKED");
    }

    @Test
    void givenValidationFailure_whenExecute_thenCompletesAsRejectedWithReason() {
        // Given
        when(lockGateway.acquire(IDEMPOTENCY_KEY)).thenReturn(lock);
        when(inboxGateway.findByCorrelationId(CORRELATION_ID)).thenReturn(Optional.empty());
        when(inboxGateway.claim(any(ProductEntryInbox.class)))
                .thenReturn(ProductEntryInbox.pending(command.getIdentifiers(), IDEMPOTENCY_KEY));
        when(chainExecutor.execute(product))
                .thenThrow(new InvalidProductException("Seller product id must be a valid UUID."));

        // When
        var status = useCase.execute(command);

        // Then
        assertEquals(ProductEntryStatus.REJECTED, status);
        verify(inboxGateway).complete(
                CORRELATION_ID,
                ProductEntryStatus.REJECTED,
                "Seller product id must be a valid UUID.");
    }

    @Test
    void givenLockNotAcquired_whenExecute_thenRethrowsException() {
        // Given
        when(lockGateway.acquire(IDEMPOTENCY_KEY)).thenThrow(new LockNotAcquiredException(IDEMPOTENCY_KEY));

        // When / Then
        assertThrows(LockNotAcquiredException.class, () -> useCase.execute(command));
        verify(inboxGateway, never()).claim(any());
        verify(chainExecutor, never()).execute(any());
    }
}

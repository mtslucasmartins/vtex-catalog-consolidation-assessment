package com.vtex.catalog.worker.dataprovider.gateway;

import com.vtex.catalog.worker.application.common.exception.LockNotAcquiredException;
import com.vtex.catalog.worker.infrastructure.config.WorkerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.integration.redis.util.RedisLockRegistry;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisDistributedLockGatewayTest {

    private static final String RESOURCE_KEY = "megastore#samsung#phone";

    @Mock
    private RedisLockRegistry lockRegistry;

    @Mock
    private Lock lock;

    private RedisDistributedLockGateway gateway;

    @BeforeEach
    void setUp() {
        var properties = new WorkerProperties();
        properties.getLock().setAcquireMaxAttempts(2);
        properties.getLock().setAcquireRetryDelayMs(1);
        properties.getLock().setProductEntryTtlMs(1000);
        gateway = new RedisDistributedLockGateway(lockRegistry, properties);
        when(lockRegistry.obtain(RESOURCE_KEY)).thenReturn(lock);
    }

    @Test
    void givenFreeLock_whenAcquire_thenReturnsSameLockInstance() throws InterruptedException {
        // Given
        when(lock.tryLock(0, TimeUnit.MILLISECONDS)).thenReturn(true);

        // When
        var acquired = gateway.acquire(RESOURCE_KEY);

        // Then
        assertSame(lock, acquired);
    }

    @Test
    void givenBusyLock_whenAcquireExhaustsRetries_thenThrowsLockNotAcquiredException() throws InterruptedException {
        // Given
        when(lock.tryLock(0, TimeUnit.MILLISECONDS)).thenReturn(false);

        // When / Then
        assertThrows(LockNotAcquiredException.class, () -> gateway.acquire(RESOURCE_KEY));
        verify(lock, atLeastOnce()).tryLock(0, TimeUnit.MILLISECONDS);
    }
}

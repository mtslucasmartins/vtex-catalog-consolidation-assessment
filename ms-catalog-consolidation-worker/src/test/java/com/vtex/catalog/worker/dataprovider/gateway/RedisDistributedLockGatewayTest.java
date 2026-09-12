package com.vtex.catalog.worker.dataprovider.gateway;

import com.vtex.catalog.worker.application.common.exception.LockNotAcquiredException;
import com.vtex.catalog.worker.infrastructure.config.WorkerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisDistributedLockGatewayTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisDistributedLockGateway gateway;

    @BeforeEach
    void setUp() {
        var properties = new WorkerProperties();
        properties.getLock().setAcquireMaxAttempts(2);
        properties.getLock().setAcquireRetryDelayMs(1);
        properties.getLock().setProductEntryTtlMs(1000);
        gateway = new RedisDistributedLockGateway(redisTemplate, properties);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void givenFreeLock_whenAcquire_thenReturnsLock() {
        // Given
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);

        // When
        var lock = gateway.acquire("megastore#samsung#phone");

        // Then
        assertNotNull(lock);
        assertEquals("megastore#samsung#phone", lock.resourceKey());
    }

    @Test
    void givenBusyLock_whenAcquireExhaustsRetries_thenThrowsLockNotAcquiredException() {
        // Given
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(false);

        // When / Then
        assertThrows(LockNotAcquiredException.class, () -> gateway.acquire("megastore#samsung#phone"));
        verify(valueOperations, atLeastOnce()).setIfAbsent(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void givenHeldLock_whenRelease_thenExecutesUnlockScript() {
        // Given
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);
        var lock = gateway.acquire("megastore#samsung#phone");

        // When
        gateway.release(lock);

        // Then
        verify(redisTemplate).execute(any(), anyList(), eq(lock.token()));
    }
}

package com.vtex.catalog.worker.dataprovider.gateway;

import com.vtex.catalog.worker.application.common.exception.LockNotAcquiredException;
import com.vtex.catalog.worker.application.gateway.DistributedLockGateway;
import com.vtex.catalog.worker.infrastructure.config.WorkerProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@Component
@RequiredArgsConstructor
public class RedisDistributedLockGateway implements DistributedLockGateway {

    private final RedisLockRegistry lockRegistry;

    private final WorkerProperties properties;

    @Override
    public Lock acquire(String resourceKey) {
        var lock = lockRegistry.obtain(resourceKey);
        var retryDelay = Duration.ofMillis(properties.getLock().getAcquireRetryDelayMs());
        var maxAttempts = properties.getLock().getAcquireMaxAttempts();

        for (var attempt = 0; attempt < maxAttempts; attempt++) {
            if (tryAcquire(lock)) {
                return lock;
            }
            if (attempt + 1 < maxAttempts) {
                sleep(retryDelay);
            }
        }

        throw new LockNotAcquiredException(resourceKey);
    }

    private static boolean tryAcquire(Lock lock) {
        try {
            return lock.tryLock(0, TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new LockNotAcquiredException("interrupted");
        }
    }

    private static void sleep(Duration delay) {
        try {
            Thread.sleep(delay.toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new LockNotAcquiredException("interrupted");
        }
    }
}

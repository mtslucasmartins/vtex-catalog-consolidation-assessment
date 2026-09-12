package com.vtex.catalog.worker.dataprovider.gateway;

import com.vtex.catalog.worker.application.common.exception.LockNotAcquiredException;
import com.vtex.catalog.worker.application.gateway.DistributedLockGateway;
import com.vtex.catalog.worker.infrastructure.config.WorkerProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedisDistributedLockGateway implements DistributedLockGateway {

    private static final String LOCK_PREFIX = "product-entry:lock:";

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            """
                    if redis.call('get', KEYS[1]) == ARGV[1] then
                        return redis.call('del', KEYS[1])
                    else
                        return 0
                    end
                    """,
            Long.class);

    private final StringRedisTemplate redisTemplate;

    private final WorkerProperties properties;

    @Override
    public DistributedLock acquire(String resourceKey) {
        var lockKey = lockKey(resourceKey);
        var token = UUID.randomUUID().toString();
        var ttl = Duration.ofMillis(properties.getLock().getProductEntryTtlMs());
        var retryDelay = Duration.ofMillis(properties.getLock().getAcquireRetryDelayMs());
        var maxAttempts = properties.getLock().getAcquireMaxAttempts();

        for (var attempt = 0; attempt < maxAttempts; attempt++) {
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, token, ttl);
            if (Boolean.TRUE.equals(acquired)) {
                return new DistributedLock(resourceKey, token);
            }
            if (attempt + 1 < maxAttempts) {
                sleep(retryDelay);
            }
        }

        throw new LockNotAcquiredException(resourceKey);
    }

    @Override
    public void release(DistributedLock lock) {
        redisTemplate.execute(
                UNLOCK_SCRIPT,
                List.of(lockKey(lock.resourceKey())),
                lock.token());
    }

    private static String lockKey(String resourceKey) {
        return LOCK_PREFIX + resourceKey;
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

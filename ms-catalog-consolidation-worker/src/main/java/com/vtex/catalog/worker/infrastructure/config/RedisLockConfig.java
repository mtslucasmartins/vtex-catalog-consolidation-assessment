package com.vtex.catalog.worker.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.integration.redis.util.RedisLockRegistry;

@Configuration
public class RedisLockConfig {

    private static final String REGISTRY_KEY = "product-entry:lock";

    @Bean(destroyMethod = "destroy")
    public RedisLockRegistry productEntryLockRegistry(
            RedisConnectionFactory connectionFactory,
            WorkerProperties properties) {
        return new RedisLockRegistry(
                connectionFactory,
                REGISTRY_KEY,
                properties.getLock().getProductEntryTtlMs());
    }
}

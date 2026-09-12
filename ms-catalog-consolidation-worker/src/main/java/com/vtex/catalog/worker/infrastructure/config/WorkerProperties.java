package com.vtex.catalog.worker.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("worker")
public class WorkerProperties {

    private Topics topics = new Topics();

    private Lock lock = new Lock();

    @Getter
    @Setter
    public static class Topics {
        private String productEntryUpdate;
    }

    @Getter
    @Setter
    public static class Lock {
        private long productEntryTtlMs = 30_000;
        private long acquireRetryDelayMs = 100;
        private int acquireMaxAttempts = 50;
    }
}

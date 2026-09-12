package com.vtex.catalog.ingester.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("ingester")
public class IngesterProperties {

    /** Streaming read chunk size when parsing large JSON files (internal buffer only). */
    private int batchSize = 1;

    private Storage storage = new Storage();

    private Topics topics = new Topics();

    private Outbox outbox = new Outbox();

    @Getter
    @Setter
    public static class Storage {
        private String bucket = "catalog-ingestions";
        private String region = "us-east-1";
        private String endpoint;
    }

    @Getter
    @Setter
    public static class Topics {
        private String productEntryUpdate;
    }

    @Getter
    @Setter
    public static class Outbox {
        private long ingestRelayDelayMs = 100;
        private int ingestRelayBatchSize = 10;
        private int maxAttempts = 5;
        private long sendTimeoutMs = 10000;
    }
}

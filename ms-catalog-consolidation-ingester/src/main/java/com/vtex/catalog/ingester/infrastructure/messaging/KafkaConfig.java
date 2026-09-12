package com.vtex.catalog.ingester.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vtex.catalog.ingester.infrastructure.config.IngesterProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    private static final long RETRY_INTERVAL_MS = 2000L;
    private static final long MAX_RETRIES = 3L;

    private final IngesterProperties properties;

    @Bean
    public NewTopic productEntryUpdateTopic() {
        return TopicBuilder.name(properties.getTopics().getProductEntryUpdate()).partitions(1).replicas(1).build();
    }

    /** Retry a record a few times with fixed back-off, then park it in {@code <topic>-dlt}. Malformed payloads are not retried. */
    @Bean
    public CommonErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> kafkaTemplate) {
        DefaultErrorHandler handler = new DefaultErrorHandler(
                new DeadLetterPublishingRecoverer(kafkaTemplate),
                new FixedBackOff(RETRY_INTERVAL_MS, MAX_RETRIES));
        handler.addNotRetryableExceptions(JsonProcessingException.class, IllegalArgumentException.class);
        handler.setRetryListeners((record, ex, attempt) -> {
            if (attempt <= MAX_RETRIES) {
                log.warn("Delivery attempt {}/{} failed for topic={} key={}: {}",
                        attempt, MAX_RETRIES + 1, record.topic(), record.key(), rootMessage(ex));
            } else {
                log.error("Retries exhausted for topic={} key={}; sent to {}-dlt. Cause: {}",
                        record.topic(), record.key(), record.topic(), rootMessage(ex));
            }
        });
        return handler;
    }

    private static String rootMessage(Throwable ex) {
        Throwable root = ex;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getClass().getSimpleName() + ": " + root.getMessage();
    }
}

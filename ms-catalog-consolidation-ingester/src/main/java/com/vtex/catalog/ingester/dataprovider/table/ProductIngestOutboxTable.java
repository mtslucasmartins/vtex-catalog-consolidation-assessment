package com.vtex.catalog.ingester.dataprovider.table;

import com.vtex.catalog.ingester.application.domain.ingestion.IngestionId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "product_ingest_outbox")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductIngestOutboxTable {

    public static final String PENDING = "PENDING";
    public static final String PROCESSING = "PROCESSING";
    public static final String DISPATCHED = "DISPATCHED";
    public static final String FAILED = "FAILED";

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "ingestion_id", nullable = false, unique = true)
    private String ingestionId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static ProductIngestOutboxTable pending(IngestionId ingestionId) {
        var now = Instant.now();
        return ProductIngestOutboxTable.builder()
                .id(UUID.randomUUID().toString())
                .ingestionId(ingestionId.getValue())
                .status(PENDING)
                .attempts(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void markProcessing() {
        this.status = PROCESSING;
        this.updatedAt = Instant.now();
    }

    public void registerFailure(String reason, int maxAttempts) {
        this.attempts++;
        this.failureReason = reason;
        this.updatedAt = Instant.now();
        if (this.attempts >= maxAttempts) {
            this.status = FAILED;
        } else {
            this.status = PENDING;
        }
    }

    public void markDispatched() {
        this.status = DISPATCHED;
        this.updatedAt = Instant.now();
    }

    public void markFailed(String reason) {
        this.status = FAILED;
        this.failureReason = reason;
        this.updatedAt = Instant.now();
    }

    public boolean isFailed() {
        return FAILED.equals(status);
    }
}

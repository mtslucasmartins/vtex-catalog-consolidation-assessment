package com.vtex.catalog.ingester.dataprovider.table;

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

@Entity
@Table(name = "product_ingest_outbox")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductIngestOutboxTable {

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
}

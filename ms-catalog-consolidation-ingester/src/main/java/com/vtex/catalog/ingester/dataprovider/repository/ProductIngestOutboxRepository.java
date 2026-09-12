package com.vtex.catalog.ingester.dataprovider.repository;

import com.vtex.catalog.ingester.dataprovider.table.ProductIngestOutboxTable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductIngestOutboxRepository extends JpaRepository<ProductIngestOutboxTable, String> {

    Optional<ProductIngestOutboxTable> findByIngestionId(String ingestionId);

    long countByStatus(String status);

    @Query("""
            SELECT message FROM ProductIngestOutboxTable message
             WHERE message.status = :status
             ORDER BY message.createdAt ASC
            """)
    List<ProductIngestOutboxTable> findByStatus(@Param("status") String status, Pageable pageable);

    @Query(value = """
            SELECT *
              FROM product_ingest_outbox
             WHERE status = 'PENDING'
             ORDER BY created_at
             FOR UPDATE SKIP LOCKED
             LIMIT :batchSize
            """, nativeQuery = true)
    List<ProductIngestOutboxTable> findPendingForUpdate(@Param("batchSize") int batchSize);
}

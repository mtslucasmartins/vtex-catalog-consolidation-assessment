package com.vtex.catalog.worker.dataprovider.repository;

import com.vtex.catalog.worker.dataprovider.table.ProductEntryInboxTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductEntryInboxRepository extends JpaRepository<ProductEntryInboxTable, String> {

    Optional<ProductEntryInboxTable> findByIdempotencyKey(String idempotencyKey);
}

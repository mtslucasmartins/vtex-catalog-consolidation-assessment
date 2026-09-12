package com.vtex.catalog.ingester.dataprovider.repository;

import com.vtex.catalog.ingester.dataprovider.table.IngestionHistoryTable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngestionHistoryRepository extends JpaRepository<IngestionHistoryTable, String> {
}

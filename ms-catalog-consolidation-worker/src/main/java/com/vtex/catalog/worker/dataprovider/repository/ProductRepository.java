package com.vtex.catalog.worker.dataprovider.repository;

import com.vtex.catalog.worker.dataprovider.table.ProductTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<ProductTable, Long> {

    Optional<ProductTable> findBySku(String sku);
}

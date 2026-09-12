package com.vtex.catalog.worker.dataprovider.repository;

import com.vtex.catalog.worker.dataprovider.table.SellerProductTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SellerProductRepository extends JpaRepository<SellerProductTable, Long> {
    @Query("""
            SELECT CASE WHEN COUNT(link) > 0 THEN true ELSE false END
              FROM SellerProductTable link
             WHERE link.sellerName = :sellerName
               AND link.sellerProductId = :sellerProductId
            """)
    boolean existsLink(@Param("sellerName") String sellerName,
                       @Param("sellerProductId") String sellerProductId);
}

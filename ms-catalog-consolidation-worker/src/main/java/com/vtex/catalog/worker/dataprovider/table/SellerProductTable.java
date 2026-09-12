package com.vtex.catalog.worker.dataprovider.table;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products_sellers")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerProductTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "seller_name", nullable = false)
    private String sellerName;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "seller_product_id", nullable = false)
    private String sellerProductId;
}

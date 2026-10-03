package com.wolfe.catalog.adminmodel;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductMediaRepository extends JpaRepository<ProductMedia, Long> {
    @Query("select m from ProductMedia m where m.product.id = :productId order by m.sortOrder asc, m.id asc")
    List<ProductMedia> findByProductIdOrderBySortOrderAscIdAsc(@Param("productId") Long productId);

    @Query(value = "select m from ProductMedia m where m.product.id = :productId order by m.sortOrder asc, m.id asc",
           countQuery = "select count(m) from ProductMedia m where m.product.id = :productId")
    Page<ProductMedia> findByProductIdOrderBySortOrderAscIdAsc(@Param("productId") Long productId, Pageable pageable);
}

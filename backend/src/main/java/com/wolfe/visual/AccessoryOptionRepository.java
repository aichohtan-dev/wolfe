package com.wolfe.visual;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccessoryOptionRepository extends JpaRepository<AccessoryOption, Long> {
    @Query("select a from AccessoryOption a where a.product.id = :productId and a.active = true order by a.type asc, a.name asc")
    List<AccessoryOption> findByProductIdAndActiveTrueOrderByTypeAscNameAsc(@Param("productId") Long productId);

    @Query("select a from AccessoryOption a where a.product.id = :productId order by a.type asc, a.name asc")
    List<AccessoryOption> findByProductIdOrderByTypeAscNameAsc(@Param("productId") Long productId);

    @Query(value = "select a from AccessoryOption a where a.product.id = :productId order by a.type asc, a.name asc",
           countQuery = "select count(a) from AccessoryOption a where a.product.id = :productId")
    Page<AccessoryOption> findByProductIdOrderByTypeAscNameAsc(@Param("productId") Long productId, Pageable pageable);
}

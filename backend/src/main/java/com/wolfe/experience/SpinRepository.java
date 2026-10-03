package com.wolfe.experience;

import java.util.*;
import org.springframework.data.jpa.repository.*;

import org.springframework.data.repository.query.Param;

public interface SpinRepository extends JpaRepository<ProductSpinFrame, Long> {
    @Query("select s from ProductSpinFrame s where s.product.id = :productId order by s.sortOrder asc")
    List<ProductSpinFrame> findByProductIdOrderBySortOrderAsc(@Param("productId") Long productId);
}

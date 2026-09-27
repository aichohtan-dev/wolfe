package com.wolfe.catalog.adminmodel;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductMediaRepository extends JpaRepository<ProductMedia, Long> {
    List<ProductMedia> findByProductIdOrderBySortOrderAscIdAsc(Long productId);
}

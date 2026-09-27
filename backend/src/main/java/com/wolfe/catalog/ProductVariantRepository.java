package com.wolfe.catalog;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProductIdAndActiveTrueOrderByOptionNameAscOptionValueAsc(Long productId);
    void deleteByProductId(Long productId);
}

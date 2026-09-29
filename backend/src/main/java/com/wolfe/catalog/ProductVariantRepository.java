package com.wolfe.catalog;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProductIdAndActiveTrueOrderBySortOrderAscIdAsc(Long productId);
    List<ProductVariant> findByProductIdOrderBySortOrderAscIdAsc(Long productId);
    List<ProductVariant> findByProductIdAndActiveTrueOrderByOptionNameAscOptionValueAsc(Long productId);
    Optional<ProductVariant> findBySku(String sku);
    Optional<ProductVariant> findByIdAndActiveTrue(Long id);

    @Query("SELECT v FROM ProductVariant v WHERE v.product.slug = :slug AND v.active = true ORDER BY v.sortOrder ASC, v.id ASC")
    List<ProductVariant> findByProductSlugActive(@Param("slug") String slug);

    void deleteByProductId(Long productId);
}

package com.wolfe.catalog;

import java.util.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    @Query("select v from ProductVariant v where v.product.id = :productId and v.active = true order by v.sortOrder asc, v.id asc")
    List<ProductVariant> findByProductIdAndActiveTrueOrderBySortOrderAscIdAsc(@Param("productId") Long productId);

    @Query("select v from ProductVariant v where v.product.id = :productId order by v.sortOrder asc, v.id asc")
    List<ProductVariant> findByProductIdOrderBySortOrderAscIdAsc(@Param("productId") Long productId);

    @Query("select v from ProductVariant v where v.product.id = :productId and v.active = true order by v.optionName asc, v.optionValue asc")
    List<ProductVariant> findByProductIdAndActiveTrueOrderByOptionNameAscOptionValueAsc(@Param("productId") Long productId);

    @Query(value = "select v from ProductVariant v where v.product.id = :productId and v.active = true order by v.optionName asc, v.optionValue asc",
           countQuery = "select count(v) from ProductVariant v where v.product.id = :productId and v.active = true")
    Page<ProductVariant> findByProductIdAndActiveTrueOrderByOptionNameAscOptionValueAsc(@Param("productId") Long productId, Pageable pageable);
    Optional<ProductVariant> findBySku(String sku);
    Optional<ProductVariant> findByIdAndActiveTrue(Long id);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from ProductVariant v where v.id = :id")
    Optional<ProductVariant> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT v FROM ProductVariant v WHERE v.product.slug = :slug AND v.active = true ORDER BY v.sortOrder ASC, v.id ASC")
    List<ProductVariant> findByProductSlugActive(@Param("slug") String slug);

    void deleteByProductId(Long productId);
}

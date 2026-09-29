package com.wolfe.catalog;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Optional<Product> findBySlug(String slug);
    Optional<Product> findBySlugIgnoreCase(String slug);
    List<Product> findAllByOrderBySortOrderAscNameAsc();

    @Query("SELECT p FROM Product p WHERE p.active = true " +
           "AND (:q IS NULL OR lower(p.name) LIKE lower(concat('%', :q, '%')) OR lower(p.description) LIKE lower(concat('%', :q, '%')) OR lower(p.slug) LIKE lower(concat('%', :q, '%')) OR lower(coalesce(p.brandName, '')) LIKE lower(concat('%', :q, '%')) OR lower(coalesce(p.subcategory, '')) LIKE lower(concat('%', :q, '%'))) " +
           "AND (:category IS NULL OR lower(p.category) = lower(:category)) " +
           "AND (:subcategory IS NULL OR lower(coalesce(p.subcategory, '')) = lower(:subcategory)) " +
           "AND (:brandId IS NULL OR p.brandId = :brandId) " +
           "AND (:brandName IS NULL OR lower(coalesce(p.brandName, '')) = lower(:brandName)) " +
           "AND (:finish IS NULL OR lower(p.finish) = lower(:finish)) " +
           "AND (:material IS NULL OR lower(p.material) = lower(:material)) " +
           "AND (:color IS NULL OR lower(p.color) = lower(:color)) " +
           "AND (:style IS NULL OR lower(p.style) = lower(:style)) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "AND (:featured IS NULL OR p.featured = :featured)")
    Page<Product> searchActivePaged(@Param("q") String q,
                                   @Param("category") String category,
                                   @Param("subcategory") String subcategory,
                                   @Param("brandId") Long brandId,
                                   @Param("brandName") String brandName,
                                   @Param("finish") String finish,
                                   @Param("material") String material,
                                   @Param("color") String color,
                                   @Param("style") String style,
                                   @Param("minPrice") BigDecimal minPrice,
                                   @Param("maxPrice") BigDecimal maxPrice,
                                   @Param("featured") Boolean featured,
                                   Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true " +
           "AND (:q IS NULL OR lower(p.name) LIKE lower(concat('%', :q, '%')) OR lower(p.description) LIKE lower(concat('%', :q, '%')) OR lower(p.slug) LIKE lower(concat('%', :q, '%')) OR lower(coalesce(p.brandName, '')) LIKE lower(concat('%', :q, '%')) OR lower(coalesce(p.subcategory, '')) LIKE lower(concat('%', :q, '%'))) " +
           "AND (:category IS NULL OR lower(p.category) = lower(:category)) " +
           "AND (:subcategory IS NULL OR lower(coalesce(p.subcategory, '')) = lower(:subcategory)) " +
           "AND (:brandId IS NULL OR p.brandId = :brandId) " +
           "AND (:brandName IS NULL OR lower(coalesce(p.brandName, '')) = lower(:brandName)) " +
           "AND (:finish IS NULL OR lower(p.finish) = lower(:finish)) " +
           "AND (:material IS NULL OR lower(p.material) = lower(:material)) " +
           "AND (:color IS NULL OR lower(p.color) = lower(:color)) " +
           "AND (:style IS NULL OR lower(p.style) = lower(:style))")
    List<Product> searchActiveList(@Param("q") String q,
                                  @Param("category") String category,
                                  @Param("subcategory") String subcategory,
                                  @Param("brandId") Long brandId,
                                  @Param("brandName") String brandName,
                                  @Param("finish") String finish,
                                  @Param("material") String material,
                                  @Param("color") String color,
                                  @Param("style") String style);

    @Query("SELECT p FROM Product p WHERE p.active = true AND (:q IS NOT NULL) " +
           "AND (lower(p.name) LIKE lower(concat('%', :q, '%')) OR lower(p.slug) LIKE lower(concat('%', :q, '%')) OR lower(p.category) LIKE lower(concat('%', :q, '%')) OR lower(coalesce(p.subcategory, '')) LIKE lower(concat('%', :q, '%')) OR lower(coalesce(p.brandName, '')) LIKE lower(concat('%', :q, '%')) OR lower(p.material) LIKE lower(concat('%', :q, '%')) OR lower(p.color) LIKE lower(concat('%', :q, '%')) OR lower(p.finish) LIKE lower(concat('%', :q, '%'))) " +
           "ORDER BY p.featured DESC, p.sortOrder ASC, p.name ASC")
    List<Product> suggestActive(@Param("q") String q, Pageable pageable);

    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.active = true ORDER BY p.category")
    List<String> findDistinctCategories();

    @Query("SELECT DISTINCT p.subcategory FROM Product p WHERE p.active = true AND p.subcategory IS NOT NULL AND p.subcategory <> '' ORDER BY p.subcategory")
    List<String> findDistinctSubcategories();

    @Query("SELECT DISTINCT p.brandName FROM Product p WHERE p.active = true AND p.brandName IS NOT NULL AND p.brandName <> '' ORDER BY p.brandName")
    List<String> findDistinctBrands();

    @Query("SELECT DISTINCT p.material FROM Product p WHERE p.active = true ORDER BY p.material")
    List<String> findDistinctMaterials();

    @Query("SELECT DISTINCT p.color FROM Product p WHERE p.active = true ORDER BY p.color")
    List<String> findDistinctColors();

    @Query("SELECT DISTINCT p.style FROM Product p WHERE p.active = true ORDER BY p.style")
    List<String> findDistinctStyles();

    @Query("SELECT DISTINCT p.finish FROM Product p WHERE p.active = true ORDER BY p.finish")
    List<String> findDistinctFinishes();

    @Query("SELECT MIN(p.price) FROM Product p WHERE p.active = true")
    BigDecimal findMinPrice();

    @Query("SELECT MAX(p.price) FROM Product p WHERE p.active = true")
    BigDecimal findMaxPrice();

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.id <> :id " +
           "AND (lower(p.category) = lower(:category) OR lower(p.material) = lower(:material) OR lower(p.style) = lower(:style)) " +
           "ORDER BY CASE WHEN lower(p.category) = lower(:category) THEN 0 ELSE 1 END, p.featured DESC, p.sortOrder ASC, p.name ASC")
    List<Product> findRelated(@Param("id") Long id, @Param("category") String category, @Param("material") String material, @Param("style") String style, Pageable pageable);
}

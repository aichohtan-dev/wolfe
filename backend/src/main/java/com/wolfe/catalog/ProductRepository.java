package com.wolfe.catalog;

import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySlug(String slug);
    List<Product> findAllByOrderBySortOrderAscNameAsc();
    @Query("select p from Product p where p.active = true and (:q is null or lower(p.name) like lower(concat('%', :q, '%')) or lower(p.description) like lower(concat('%', :q, '%'))) and (:category is null or lower(p.category) = lower(:category)) and (:finish is null or lower(p.finish) = lower(:finish)) and (:material is null or lower(p.material) = lower(:material)) and (:color is null or lower(p.color) = lower(:color)) and (:style is null or lower(p.style) = lower(:style))")
    List<Product> searchActive(@Param("q")String q, @Param("category")String category, @Param("finish")String finish, @Param("material")String material,
    @Param("color")String color,
    @Param("style")String style);
    @Query("select p from Product p where p.active = true and (:q is not null) and (lower(p.name) like lower(concat(:q, '%')) or lower(p.slug) like lower(concat(:q, '%')) or lower(p.category) like lower(concat(:q, '%')) or lower(p.material) like lower(concat(:q, '%')) or lower(p.color) like lower(concat(:q, '%')) or lower(p.finish) like lower(concat(:q, '%'))) order by p.featured desc, p.sortOrder asc, p.name asc")
    List<Product> suggestActive(@Param("q")String q, org.springframework.data.domain.Pageable pageable);
    @Query("select distinct p.category from Product p where p.active = true order by p.category") List<String> findDistinctCategories();
    @Query("select distinct p.material from Product p where p.active = true order by p.material") List<String> findDistinctMaterials();
    @Query("select distinct p.color from Product p where p.active = true order by p.color") List<String> findDistinctColors();
    @Query("select distinct p.style from Product p where p.active = true order by p.style") List<String> findDistinctStyles();
    @Query("select distinct p.finish from Product p where p.active = true order by p.finish") List<String> findDistinctFinishes();
    @Query("select p from Product p where p.active = true and p.id<>:id and (lower(p.category) = lower(:category) or lower(p.material) = lower(:material) or lower(p.style) = lower(:style)) order by case when lower(p.category) = lower(:category) then 0 else 1 end, p.featured desc, p.sortOrder asc, p.name asc") List<Product> findRelated(@Param("id")Long id, @Param("category")String category, @Param("material")String material, @Param("style")String style,
    org.springframework.data.domain.Pageable pageable);
}

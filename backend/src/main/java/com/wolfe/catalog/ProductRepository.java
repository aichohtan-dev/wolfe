package com.wolfe.catalog;

import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySlug(String slug);
    Optional<Product> findBySlugIgnoreCase(String slug);
    List<Product> findAllByOrderBySortOrderAscNameAsc();
    @Query("select p from Product p where p.active = true and (cast(:q as string) is null or lower(p.name) like lower(concat('%', cast(:q as string), '%')) or lower(p.description) like lower(concat('%', cast(:q as string), '%'))) and (cast(:category as string) is null or lower(p.category) = lower(cast(:category as string))) and (cast(:finish as string) is null or lower(p.finish) = lower(cast(:finish as string))) and (cast(:material as string) is null or lower(p.material) = lower(cast(:material as string))) and (cast(:color as string) is null or lower(p.color) = lower(cast(:color as string))) and (cast(:style as string) is null or lower(p.style) = lower(cast(:style as string)))")
    List<Product> searchActive(@Param("q")String q, @Param("category")String category, @Param("finish")String finish, @Param("material")String material,
    @Param("color")String color,
    @Param("style")String style);
    @Query("select p from Product p where p.active = true and (cast(:q as string) is not null) and (lower(p.name) like lower(concat(cast(:q as string), '%')) or lower(p.slug) like lower(concat(cast(:q as string), '%')) or lower(p.category) like lower(concat(cast(:q as string), '%')) or lower(p.material) like lower(concat(cast(:q as string), '%')) or lower(p.color) like lower(concat(cast(:q as string), '%')) or lower(p.finish) like lower(concat(cast(:q as string), '%'))) order by p.featured desc, p.sortOrder asc, p.name asc")
    List<Product> suggestActive(@Param("q")String q, org.springframework.data.domain.Pageable pageable);
    @Query("select distinct p.category from Product p where p.active = true order by p.category") List<String> findDistinctCategories();
    @Query("select distinct p.material from Product p where p.active = true order by p.material") List<String> findDistinctMaterials();
    @Query("select distinct p.color from Product p where p.active = true order by p.color") List<String> findDistinctColors();
    @Query("select distinct p.style from Product p where p.active = true order by p.style") List<String> findDistinctStyles();
    @Query("select distinct p.finish from Product p where p.active = true order by p.finish") List<String> findDistinctFinishes();
    @Query("select p from Product p where p.active = true and p.id<>:id and (lower(p.category) = lower(cast(:category as string)) or lower(p.material) = lower(cast(:material as string)) or lower(p.style) = lower(cast(:style as string))) order by case when lower(p.category) = lower(cast(:category as string)) then 0 else 1 end, p.featured desc, p.sortOrder asc, p.name asc") List<Product> findRelated(@Param("id")Long id, @Param("category")String category, @Param("material")String material, @Param("style")String style,
    org.springframework.data.domain.Pageable pageable);
}

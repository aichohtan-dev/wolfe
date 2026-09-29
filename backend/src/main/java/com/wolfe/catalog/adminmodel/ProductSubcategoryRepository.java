package com.wolfe.catalog.adminmodel;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductSubcategoryRepository extends JpaRepository<ProductSubcategory, Long> {
    List<ProductSubcategory> findByCategoryNameAndActiveTrueOrderBySortOrderAscNameAsc(String categoryName);
    List<ProductSubcategory> findByActiveTrueOrderByCategoryNameAscSortOrderAscNameAsc();
    List<ProductSubcategory> findAllByOrderByCategoryNameAscSortOrderAscNameAsc();
    Optional<ProductSubcategory> findByCategoryNameAndSlug(String categoryName, String slug);
    Optional<ProductSubcategory> findByCategoryNameAndName(String categoryName, String name);
}

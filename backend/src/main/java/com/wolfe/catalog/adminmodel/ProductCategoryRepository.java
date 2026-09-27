package com.wolfe.catalog.adminmodel;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    Optional<ProductCategory> findByNameIgnoreCase(String name);
    List<ProductCategory> findAllByOrderByNameAsc();
}

package com.wolfe.catalog.brand;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    Optional<Brand> findBySlug(String slug);
    Optional<Brand> findByNameIgnoreCase(String name);
    List<Brand> findByActiveTrueOrderBySortOrderAscNameAsc();
    List<Brand> findAllByOrderBySortOrderAscNameAsc();
    boolean existsByNameIgnoreCase(String name);
    boolean existsBySlugIgnoreCase(String slug);
}

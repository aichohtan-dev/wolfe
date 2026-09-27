package com.wolfe.catalog.adminmodel;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCollectionRepository extends JpaRepository<ProductCollection, Long> {
    Optional<ProductCollection> findByNameIgnoreCase(String name);
    List<ProductCollection> findAllByOrderByNameAsc();
}

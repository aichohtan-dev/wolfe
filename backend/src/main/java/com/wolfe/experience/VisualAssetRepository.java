package com.wolfe.experience;

import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface VisualAssetRepository extends JpaRepository<ProductVisualAsset, Long> {
    @Query("SELECT a FROM ProductVisualAsset a WHERE a.product.id = :productId")
    Optional<ProductVisualAsset> findByProductId(@org.springframework.data.repository.query.Param("productId") Long productId);
}

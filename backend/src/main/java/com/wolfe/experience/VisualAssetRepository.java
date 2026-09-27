package com.wolfe.experience;

import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface VisualAssetRepository extends JpaRepository<ProductVisualAsset, Long> {
    Optional<ProductVisualAsset> findByProductId(Long productId);
}

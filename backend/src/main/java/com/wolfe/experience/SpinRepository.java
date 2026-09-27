package com.wolfe.experience;

import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface SpinRepository extends JpaRepository<ProductSpinFrame, Long> {
    List<ProductSpinFrame> findByProductIdOrderBySortOrderAsc(Long productId);
}

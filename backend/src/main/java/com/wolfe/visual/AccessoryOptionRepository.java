package com.wolfe.visual;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessoryOptionRepository extends JpaRepository<AccessoryOption, Long> {
    List<AccessoryOption> findByProductIdAndActiveTrueOrderByTypeAscNameAsc(Long productId);
    List<AccessoryOption> findByProductIdOrderByTypeAscNameAsc(Long productId);
}

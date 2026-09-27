package com.wolfe.customdesign;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomDesignRequestRepository extends JpaRepository<CustomDesignRequest, Long> {
    List<CustomDesignRequest> findByCustomerIdOrderByCreatedAtDesc(Long id);
}

package com.wolfe.experience;

import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface RecentRepository extends JpaRepository<RecentlyViewed, Long> {
    Optional<RecentlyViewed> findByCustomerIdAndProductId(Long customerId, Long productId);
    List<RecentlyViewed> findTop8ByCustomerIdOrderByViewedAtDesc(Long customerId);
}

package com.wolfe.experience;

import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface StockSubscriptionRepository extends JpaRepository<BackInStockSubscription, Long> {
    Optional<BackInStockSubscription> findByCustomerIdAndProductId(Long customerId, Long productId);
    List<BackInStockSubscription> findByProductIdAndActiveTrue(Long productId);
}

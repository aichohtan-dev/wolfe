package com.wolfe.cart;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCustomerId(Long customerId);
    @Query("""
        select c from CartItem c
        where c.customerId = :customerId and c.productId = :productId
          and ((:variantId is null and c.variantId is null) or c.variantId = :variantId)
          and ((:bundleId is null and c.bundleId is null) or c.bundleId = :bundleId)
          and ((:configurationToken is null and c.configurationToken is null) or c.configurationToken = :configurationToken)
        """)
    Optional<CartItem> findExact(@Param("customerId") Long customerId,
                                 @Param("productId") Long productId,
                                 @Param("variantId") Long variantId,
                                 @Param("bundleId") Long bundleId,
                                 @Param("configurationToken") String configurationToken);
    long countByCustomerId(Long customerId);
    void deleteByCustomerId(Long customerId);
    long deleteByProductId(Long productId);
}

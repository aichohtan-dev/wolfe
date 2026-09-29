package com.wolfe.retailer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RetailerMarginRuleRepository extends JpaRepository<RetailerMarginRule, Long> {
    List<RetailerMarginRule> findByRetailerId(Long retailerId);
    List<RetailerMarginRule> findByActiveTrueOrderByPriorityDesc();

    @Query("SELECT r FROM RetailerMarginRule r WHERE r.active = TRUE AND " +
           "(r.retailerId IS NULL OR r.retailerId = :retailerId) AND " +
           "(r.variantId IS NULL OR r.variantId = :variantId) AND " +
           "(r.productId IS NULL OR r.productId = :productId) AND " +
           "(r.category IS NULL OR LOWER(r.category) = LOWER(:category)) " +
           "ORDER BY r.priority DESC, r.id DESC")
    List<RetailerMarginRule> findMatchingRules(
            @Param("retailerId") Long retailerId,
            @Param("variantId") Long variantId,
            @Param("productId") Long productId,
            @Param("category") String category
    );
}

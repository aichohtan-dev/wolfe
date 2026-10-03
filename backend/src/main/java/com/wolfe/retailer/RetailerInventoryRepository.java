package com.wolfe.retailer;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Collection;
import java.util.Optional;

@Repository
public interface RetailerInventoryRepository extends JpaRepository<RetailerInventory, Long> {
    List<RetailerInventory> findByRetailerId(Long retailerId);
    Page<RetailerInventory> findByRetailerId(Long retailerId, Pageable pageable);

    Optional<RetailerInventory> findByRetailerIdAndSku(Long retailerId, String sku);

    List<RetailerInventory> findByRetailerIdAndSkuIn(Long retailerId, Collection<String> skus);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM RetailerInventory i WHERE i.retailerId = :retailerId AND i.sku = :sku")
    Optional<RetailerInventory> findByRetailerIdAndSkuForUpdate(@Param("retailerId") Long retailerId, @Param("sku") String sku);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM RetailerInventory i WHERE i.retailerId = :retailerId AND i.productId = :productId AND (:variantId IS NULL AND i.variantId IS NULL OR i.variantId = :variantId)")
    Optional<RetailerInventory> findByRetailerIdAndProductAndVariantForUpdate(@Param("retailerId") Long retailerId, @Param("productId") Long productId, @Param("variantId") Long variantId);

    @Query("SELECT i FROM RetailerInventory i WHERE i.retailerId = :retailerId AND (:query IS NULL OR :query = '' OR LOWER(i.sku) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<RetailerInventory> searchByRetailer(@Param("retailerId") Long retailerId, @Param("query") String query, Pageable pageable);

    @Query("SELECT i FROM RetailerInventory i WHERE i.sku = :sku AND i.availableStock >= :minQty")
    List<RetailerInventory> findEligibleWithStock(@Param("sku") String sku, @Param("minQty") int minQty);

    @Query("SELECT i FROM RetailerInventory i WHERE i.retailerId = :retailerId AND i.availableStock <= i.lowStockThreshold")
    List<RetailerInventory> findLowStockItems(@Param("retailerId") Long retailerId);
}

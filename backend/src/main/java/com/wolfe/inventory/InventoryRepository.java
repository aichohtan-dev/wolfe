package com.wolfe.inventory;

import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProductId(Long productId);
    @Query("select count(i) from Inventory i where (i.quantity - i.reserved) <= :threshold")
    long countByAvailableLessThanEqual(@Param("threshold") int threshold);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inventory i where i.productId = :productId")
    Optional<Inventory> findByProductIdForUpdate(@Param("productId") Long productId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO inventory (product_id, quantity, reserved, updated_at) VALUES (:productId, :quantity, 0, NOW()) ON CONFLICT (product_id) DO NOTHING", nativeQuery = true)
    void insertDefault(@Param("productId") Long productId, @Param("quantity") int quantity);
}

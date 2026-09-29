package com.wolfe.retailer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {
    List<InventoryMovement> findByRetailerIdOrderByCreatedAtDesc(Long retailerId);
    Page<InventoryMovement> findByRetailerIdOrderByCreatedAtDesc(Long retailerId, Pageable pageable);
    List<InventoryMovement> findByOrderIdOrderByCreatedAtDesc(String orderId);
    List<InventoryMovement> findByRetailerIdAndSkuOrderByCreatedAtDesc(Long retailerId, String sku);
}

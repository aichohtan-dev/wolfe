package com.wolfe.inventory;

import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByProductId(Long productId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Inventory> findByProductIdForUpdate(Long productId);
}

package com.wolfe.retailer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RetailerSettlementRepository extends JpaRepository<RetailerSettlement, Long> {
    Optional<RetailerSettlement> findByOrderId(String orderId);
    Optional<RetailerSettlement> findByOrderIdAndRetailerId(String orderId, Long retailerId);
    List<RetailerSettlement> findByRetailerId(Long retailerId);
    Page<RetailerSettlement> findByRetailerId(Long retailerId, Pageable pageable);
    List<RetailerSettlement> findByRetailerIdAndStatus(Long retailerId, String status);

    @Query("SELECT s FROM RetailerSettlement s WHERE (:retailerId IS NULL OR s.retailerId = :retailerId) AND (:status IS NULL OR :status = '' OR s.status = :status) ORDER BY s.createdAt DESC")
    Page<RetailerSettlement> searchSettlements(@Param("retailerId") Long retailerId, @Param("status") String status, Pageable pageable);
}

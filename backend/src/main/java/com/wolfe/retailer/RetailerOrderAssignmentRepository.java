package com.wolfe.retailer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface RetailerOrderAssignmentRepository extends JpaRepository<RetailerOrderAssignment, Long> {
    List<RetailerOrderAssignment> findByOrderId(String orderId);
    Optional<RetailerOrderAssignment> findTopByOrderIdOrderByIdDesc(String orderId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from RetailerOrderAssignment a where a.id = :id")
    Optional<RetailerOrderAssignment> findByIdForUpdate(@Param("id") Long id);
    List<RetailerOrderAssignment> findByRetailerId(Long retailerId);
    Page<RetailerOrderAssignment> findByRetailerId(Long retailerId, Pageable pageable);
    List<RetailerOrderAssignment> findByRetailerIdAndStatus(Long retailerId, String status);
    Optional<RetailerOrderAssignment> findTopByOrderIdAndRetailerIdOrderByIdDesc(String orderId, Long retailerId);
    List<RetailerOrderAssignment> findByStatusAndAssignedAtBefore(String status, java.time.OffsetDateTime cutoff);
}

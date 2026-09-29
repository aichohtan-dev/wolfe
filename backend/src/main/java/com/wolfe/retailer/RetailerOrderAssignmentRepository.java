package com.wolfe.retailer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RetailerOrderAssignmentRepository extends JpaRepository<RetailerOrderAssignment, Long> {
    List<RetailerOrderAssignment> findByOrderId(String orderId);
    Optional<RetailerOrderAssignment> findTopByOrderIdOrderByIdDesc(String orderId);
    List<RetailerOrderAssignment> findByRetailerId(Long retailerId);
    Page<RetailerOrderAssignment> findByRetailerId(Long retailerId, Pageable pageable);
    List<RetailerOrderAssignment> findByRetailerIdAndStatus(Long retailerId, String status);
    Optional<RetailerOrderAssignment> findByOrderIdAndRetailerId(String orderId, Long retailerId);
}

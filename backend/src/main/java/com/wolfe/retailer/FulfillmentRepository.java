package com.wolfe.retailer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FulfillmentRepository extends JpaRepository<Fulfillment, Long> {
    Optional<Fulfillment> findByOrderId(String orderId);
    Optional<Fulfillment> findByOrderIdAndRetailerId(String orderId, Long retailerId);
    List<Fulfillment> findByRetailerId(Long retailerId);
    Page<Fulfillment> findByRetailerId(Long retailerId, Pageable pageable);
    List<Fulfillment> findByRetailerIdAndStatus(Long retailerId, String status);
}

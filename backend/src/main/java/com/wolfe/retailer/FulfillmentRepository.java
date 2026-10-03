package com.wolfe.retailer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FulfillmentRepository extends JpaRepository<Fulfillment, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select f from Fulfillment f where f.orderId = :orderId order by f.id desc")
    List<Fulfillment> findByOrderIdForUpdateRows(@org.springframework.data.repository.query.Param("orderId") String orderId);

    default Optional<Fulfillment> findByOrderIdForUpdate(String orderId) {
        return findByOrderIdForUpdateRows(orderId).stream().findFirst();
    }
    Optional<Fulfillment> findTopByOrderIdOrderByIdDesc(String orderId);
    default Optional<Fulfillment> findByOrderId(String orderId) { return findTopByOrderIdOrderByIdDesc(orderId); }
    Optional<Fulfillment> findTopByOrderIdAndRetailerIdOrderByIdDesc(String orderId, Long retailerId);
    List<Fulfillment> findByRetailerId(Long retailerId);
    Page<Fulfillment> findByRetailerId(Long retailerId, Pageable pageable);
    List<Fulfillment> findByRetailerIdAndStatus(Long retailerId, String status);
}

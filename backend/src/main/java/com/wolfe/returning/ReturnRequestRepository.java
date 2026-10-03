package com.wolfe.returning;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select r from ReturnRequest r where r.id = :id")
    Optional<ReturnRequest> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") Long id);
    List<ReturnRequest> findAllByOrderIdOrderByCreatedAtAsc(String orderId);
    List<ReturnRequest> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}

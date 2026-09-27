package com.wolfe.returning;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {
    Optional<ReturnRequest> findByOrderId(String orderId);
    List<ReturnRequest> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}

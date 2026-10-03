package com.wolfe.quote;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuoteRequestRepository extends JpaRepository<QuoteRequest, Long> {
    List<QuoteRequest> findByCustomerIdOrderByCreatedAtDesc(Long id);
    Page<QuoteRequest> findByCustomerIdOrderByCreatedAtDesc(Long id, Pageable pageable);
    long countByStatusNot(String status);
}

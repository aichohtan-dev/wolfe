package com.wolfe.quote;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuoteRequestRepository extends JpaRepository<QuoteRequest, Long> {
    List<QuoteRequest> findByCustomerIdOrderByCreatedAtDesc(Long id);
}

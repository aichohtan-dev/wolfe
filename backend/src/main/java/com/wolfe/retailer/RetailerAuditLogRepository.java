package com.wolfe.retailer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RetailerAuditLogRepository extends JpaRepository<RetailerAuditLog, Long> {
    List<RetailerAuditLog> findByEntityNameAndEntityIdOrderByCreatedAtDesc(String entityName, String entityId);
    Page<RetailerAuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}

package com.wolfe.notification;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerNotificationRepository extends JpaRepository<CustomerNotification, Long> {
    List<CustomerNotification> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    long countByCustomerIdAndReadAtIsNull(Long customerId);
}

package com.wolfe.order;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findAllByOrderByCreatedAtDesc();
    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}

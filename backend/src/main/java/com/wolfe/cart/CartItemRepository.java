package com.wolfe.cart;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, CartItem.Key> {
    List<CartItem> findByCustomerId(Long customerId);
}

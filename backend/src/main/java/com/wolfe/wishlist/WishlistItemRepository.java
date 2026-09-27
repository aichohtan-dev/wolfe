package com.wolfe.wishlist;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, WishlistItem.Key> {
    List<WishlistItem> findByCustomerId(Long customerId);
}

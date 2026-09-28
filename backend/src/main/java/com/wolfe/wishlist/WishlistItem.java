package com.wolfe.wishlist;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "wishlist_items")
@IdClass(WishlistItem.Key.class)
public class WishlistItem {
    @Id
    @Column(name = "customer_id") private Long customerId;
    @Id
    @Column(name = "product_id") private Long productId;
    protected WishlistItem() {
    }
    public WishlistItem(Long c, Long p) {
        customerId = c;
        productId = p;
    }
    public Long getCustomerId() {
        return customerId;
    }
    public Long getProductId() {
        return productId;
    }
    public static class Key implements Serializable {
        public Long customerId;
        public Long productId;
        public Key() {
        }
        public Key(Long c, Long p) {
            customerId = c;
            productId = p;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key k)) return false;
            return java.util.Objects.equals(customerId, k.customerId) && java.util.Objects.equals(productId, k.productId);
        }
        @Override
        public int hashCode() {
            return java.util.Objects.hash(customerId, productId);
        }
    }
}

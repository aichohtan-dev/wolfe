package com.wolfe.cart;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "cart_items")
@IdClass(CartItem.Key.class)
public class CartItem {
    @Id
    @Column(name = "customer_id") private Long customerId;
    @Id
    @Column(name = "product_id") private Long productId;
    @Column(nullable = false) private int quantity;
    protected CartItem() {
    }
    public CartItem(Long customerId, Long productId, int quantity) {
        this.customerId = customerId;
        this.productId = productId;
        this.quantity = quantity;
    }
    public Long getCustomerId() {
        return customerId;
    }
    public Long getProductId() {
        return productId;
    }
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
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

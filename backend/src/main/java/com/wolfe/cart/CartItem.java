package com.wolfe.cart;

import jakarta.persistence.*;

@Entity
@Table(name = "cart_items")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "variant_id")
    private Long variantId;

    @Column(name = "bundle_id")
    private Long bundleId;

    @Column(name = "configuration_token", length = 64)
    private String configurationToken;

    @Column(nullable = false)
    private int quantity;

    protected CartItem() {}

    public CartItem(Long customerId, Long productId, Long variantId, int quantity) {
        this(customerId, productId, variantId, null, null, quantity);
    }

    public CartItem(Long customerId, Long productId, Long variantId, Long bundleId, String configurationToken, int quantity) {
        this.customerId = customerId;
        this.productId = productId;
        this.variantId = variantId;
        this.bundleId = bundleId;
        this.configurationToken = configurationToken;
        this.quantity = quantity;
    }

    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public Long getProductId() { return productId; }
    public Long getVariantId() { return variantId; }
    public Long getBundleId() { return bundleId; }
    public String getConfigurationToken() { return configurationToken; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}

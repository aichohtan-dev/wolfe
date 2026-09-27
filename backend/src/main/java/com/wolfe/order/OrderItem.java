package com.wolfe.order;

import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "order_id") private String orderId;
    @Column(name = "product_id") private Long productId;
    private int quantity;
    @Column(name = "unit_price") private long unitPrice;
    @Column(name = "product_name", nullable = false) private String productName;
    @Column(name = "configuration_token", length = 32) private String configurationToken;
    @Column(name = "configuration_json", length = 12000) private String configurationJson;
    @Column(name = "bundle_id") private Long bundleId;
    @Column(name = "bundle_discount", nullable = false) private long bundleDiscount;
    protected OrderItem() {
    }
    public OrderItem(String orderId, Long productId, String productName, int quantity, long unitPrice, String configurationToken, String configurationJson,
    Long bundleId,
    long bundleDiscount) {
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.configurationToken = configurationToken;
        this.configurationJson = configurationJson;
        this.bundleId = bundleId;
        this.bundleDiscount = bundleDiscount;
    }
    public Long getId() {
        return id;
    }
    public String getOrderId() {
        return orderId;
    }
    public Long getProductId() {
        return productId;
    }
    public String getProductName() {
        return productName;
    }
    public int getQuantity() {
        return quantity;
    }
    public long getUnitPrice() {
        return unitPrice;
    }
    public String getConfigurationToken() {
        return configurationToken;
    }
    public String getConfigurationJson() {
        return configurationJson;
    }
    public Long getBundleId() {
        return bundleId;
    }
    public long getBundleDiscount() {
        return bundleDiscount;
    }
}

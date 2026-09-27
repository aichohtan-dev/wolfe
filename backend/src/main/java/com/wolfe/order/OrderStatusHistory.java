package com.wolfe.order;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "order_status_history")
public class OrderStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "order_id", nullable = false) private String orderId;
    @Column(nullable = false) private String status;
    @Column(length = 500) private String note;
    @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();
    protected OrderStatusHistory() {
    }
    public OrderStatusHistory(String orderId, String status, String note) {
        this.orderId = orderId;
        this.status = status;
        this.note = note;
    }
    public Long getId() {
        return id;
    }
    public String getOrderId() {
        return orderId;
    }
    public String getStatus() {
        return status;
    }
    public String getNote() {
        return note;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
}

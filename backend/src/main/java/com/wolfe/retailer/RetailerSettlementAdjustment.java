package com.wolfe.retailer;
import jakarta.persistence.*; import java.time.OffsetDateTime;
@Entity @Table(name="retailer_settlement_adjustments", indexes={@Index(name="idx_settlement_adj_order",columnList="order_id"),@Index(name="idx_settlement_adj_settlement",columnList="settlement_id")})
public class RetailerSettlementAdjustment {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="settlement_id",nullable=false) private Long settlementId; @Column(name="order_id",nullable=false,length=50) private String orderId;
 @Column(name="retailer_id",nullable=false) private Long retailerId; @Column(nullable=false) private long amount; @Column(nullable=false,length=80) private String type;
 @Column(length=500) private String note; @Column(name="created_by",nullable=false,length=150) private String createdBy; @Column(name="created_at",nullable=false) private OffsetDateTime createdAt=OffsetDateTime.now();
 protected RetailerSettlementAdjustment(){}
 public RetailerSettlementAdjustment(Long settlementId,String orderId,Long retailerId,long amount,String type,String note,String createdBy){this.settlementId=settlementId;this.orderId=orderId;this.retailerId=retailerId;this.amount=amount;this.type=type;this.note=note;this.createdBy=createdBy==null?"SYSTEM":createdBy;}
 public Long getId(){return id;} public Long getSettlementId(){return settlementId;} public String getOrderId(){return orderId;} public Long getRetailerId(){return retailerId;} public long getAmount(){return amount;} public String getType(){return type;} public String getNote(){return note;} public String getCreatedBy(){return createdBy;} public OffsetDateTime getCreatedAt(){return createdAt;}
}

package com.wolfe.order;

import java.util.List;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :id")
    java.util.Optional<Order> findByIdForUpdate(@Param("id") String id);

    List<Order> findAllByOrderByCreatedAtDesc();
    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    long countByStatus(String status);
    @Query("select o from Order o where o.status = 'CONFIRMED' and not exists (select a.id from RetailerOrderAssignment a where a.orderId = o.id and a.status in ('ASSIGNED','ACCEPTED')) and not exists (select f.id from Fulfillment f where f.orderId = o.id and f.status in ('ASSIGNED','ACCEPTED','PACKED','READY_FOR_DELIVERY','OUT_FOR_DELIVERY','DELIVERED')) order by o.createdAt asc")
    List<Order> findConfirmedAwaitingRetailerAllocation(org.springframework.data.domain.Pageable pageable);

    @Query("select case when count(i) > 0 then true else false end from Order o join OrderItem i on i.orderId = o.id where o.customerId = :customerId and o.status = 'DELIVERED' and i.productId = :productId")
    boolean existsDeliveredProductForCustomer(@Param("customerId") Long customerId, @Param("productId") Long productId);
    java.util.Optional<Order> findByCustomerIdAndIdempotencyKey(Long customerId, String idempotencyKey);
    long countByCustomerIdAndPaymentMethodAndStatusIn(Long customerId, String paymentMethod, java.util.Collection<String> statuses);
    long countByPhoneAndPaymentMethodAndStatusIn(String phone, String paymentMethod, java.util.Collection<String> statuses);
    long countByAddressIgnoreCaseAndPaymentMethodAndStatusIn(String address, String paymentMethod, java.util.Collection<String> statuses);
    long countByCustomerIdAndCouponCodeAndStatusNot(Long customerId, String couponCode, String status);
    @org.springframework.data.jpa.repository.Query("select coalesce(sum(o.total),0) from Order o where o.status <> 'CANCELLED'")
    long sumRevenueExcludingCancelled();
}

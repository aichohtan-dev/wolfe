package com.wolfe.retailer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RetailerRepository extends JpaRepository<Retailer, Long> {
    Optional<Retailer> findByEmail(String email);
    Optional<Retailer> findByUserId(Long userId);
    List<Retailer> findByStatusAndVerificationStatus(String status, String verificationStatus);
    List<Retailer> findByCityIgnoreCase(String city);

    @Query("SELECT r FROM Retailer r WHERE (:query IS NULL OR :query = '' OR LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(r.city) LIKE LOWER(CONCAT('%', :query, '%')) OR r.pincode LIKE CONCAT('%', :query, '%')) AND (:status IS NULL OR :status = '' OR r.status = :status)")
    Page<Retailer> searchRetailers(@Param("query") String query, @Param("status") String status, Pageable pageable);
}

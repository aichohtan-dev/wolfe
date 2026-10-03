package com.wolfe.review;

import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    Page<ProductReview> findByProductIdAndStatus(Long p, String s, Pageable pageable);
    Optional<ProductReview> findByProductIdAndCustomerId(Long p, Long c);
    long countByStatus(String status);
    @org.springframework.data.jpa.repository.Query("select coalesce(avg(r.rating),0) from ProductReview r where r.productId = :productId and r.status = :status")
    double averageRating(@org.springframework.data.repository.query.Param("productId") Long productId, @org.springframework.data.repository.query.Param("status") String status);
}

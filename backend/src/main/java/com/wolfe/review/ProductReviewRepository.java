package com.wolfe.review;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    List<ProductReview> findByProductIdAndStatusOrderByCreatedAtDesc(Long p, String s);
    Optional<ProductReview> findByProductIdAndCustomerId(Long p, Long c);
}

package com.wolfe.security;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RefreshToken r where r.tokenHash = :tokenHash")
    Optional<RefreshToken> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);
    @org.springframework.data.jpa.repository.Modifying
    @Query("update RefreshToken r set r.revokedAt = :now where r.customerId = :customerId and r.revokedAt is null")
    int revokeAllByCustomerId(@Param("customerId") Long customerId, @Param("now") java.time.Instant now);

    void deleteByCustomerId(Long customerId);
    long deleteByExpiresAtBefore(java.time.Instant cutoff);
    long deleteByRevokedAtBefore(java.time.Instant cutoff);
    long countByCustomerIdAndRevokedAtIsNullAndExpiresAtAfter(Long customerId, java.time.Instant now);
    java.util.List<RefreshToken> findByCustomerIdAndRevokedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(Long customerId, java.time.Instant now);
    java.util.List<RefreshToken> findTop5ByCustomerIdAndRevokedAtIsNullAndExpiresAtAfterOrderByExpiresAtAsc(Long customerId, java.time.Instant now);
}

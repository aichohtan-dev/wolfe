package com.wolfe.customer;

import java.time.Instant;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountTokenRepository extends JpaRepository<AccountToken,Long> {
    Optional<AccountToken> findByTokenHashAndType(String tokenHash, String type);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from AccountToken t where t.tokenHash = :tokenHash and t.type = :type")
    Optional<AccountToken> findByTokenHashAndTypeForUpdate(@Param("tokenHash") String tokenHash, @Param("type") String type);
    void deleteByExpiresAtBefore(Instant now);
    void deleteByCustomerIdAndType(Long customerId, String type);
}

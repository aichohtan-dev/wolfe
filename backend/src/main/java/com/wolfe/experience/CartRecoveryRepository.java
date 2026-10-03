package com.wolfe.experience;

import java.time.Instant;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface CartRecoveryRepository extends JpaRepository<CartRecovery, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CartRecovery c where c.reminderSent = false and c.lastActivity < :cutoff and (c.reminderClaimedAt is null or c.reminderClaimedAt < :stale) order by c.lastActivity asc")
    List<CartRecovery> claimCandidates(@Param("cutoff") Instant cutoff, @Param("stale") Instant stale, org.springframework.data.domain.Pageable pageable);
}

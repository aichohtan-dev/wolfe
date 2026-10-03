package com.wolfe.experience;

import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;

public interface ConfigurationRepository extends JpaRepository<ProductConfiguration, Long> {
    Optional<ProductConfiguration> findByShareToken(String token);
    List<ProductConfiguration> findByShareTokenIn(Collection<String> tokens);
    @Modifying
    @Query("delete from ProductConfiguration c where c.customerId is null and c.createdAt < :cutoff")
    int deleteExpiredAnonymous(@Param("cutoff") Instant cutoff);
}

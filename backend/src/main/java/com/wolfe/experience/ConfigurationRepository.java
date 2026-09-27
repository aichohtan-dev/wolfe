package com.wolfe.experience;

import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface ConfigurationRepository extends JpaRepository<ProductConfiguration, Long> {
    Optional<ProductConfiguration> findByShareToken(String token);
}

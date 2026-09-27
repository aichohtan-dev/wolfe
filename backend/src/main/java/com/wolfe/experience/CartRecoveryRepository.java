package com.wolfe.experience;

import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface CartRecoveryRepository extends JpaRepository<CartRecovery, Long> {
    List<CartRecovery> findByReminderSentFalseAndLastActivityBefore(java.time.Instant cutoff);
}

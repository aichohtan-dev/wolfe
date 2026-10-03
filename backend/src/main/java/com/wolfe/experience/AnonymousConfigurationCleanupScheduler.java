package com.wolfe.experience;

import java.time.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AnonymousConfigurationCleanupScheduler {
    private final ConfigurationRepository configurations;
    private final com.wolfe.ops.SchedulerLockService schedulerLock;
    private final int configurationTtlDays;
    public AnonymousConfigurationCleanupScheduler(ConfigurationRepository configurations, com.wolfe.ops.SchedulerLockService schedulerLock,
            @Value("${WOLFE_CONFIGURATION_TTL_DAYS:30}") int configurationTtlDays) {
        if (configurationTtlDays < 1 || configurationTtlDays > 3650) throw new IllegalArgumentException("WOLFE_CONFIGURATION_TTL_DAYS must be between 1 and 3650");
        this.configurations = configurations; this.schedulerLock = schedulerLock; this.configurationTtlDays = configurationTtlDays;
    }
    @Scheduled(fixedDelayString = "PT6H")
    @Transactional
    public void purgeExpiredAnonymousConfigurations() {
        schedulerLock.runIfLeader(0x574F4C4645434F4EL, () -> configurations.deleteExpiredAnonymous(Instant.now().minus(Duration.ofDays(configurationTtlDays))));
    }
}

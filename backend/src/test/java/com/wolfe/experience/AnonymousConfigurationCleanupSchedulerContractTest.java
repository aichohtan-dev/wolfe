package com.wolfe.experience;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class AnonymousConfigurationCleanupSchedulerContractTest {
    @Test
    void cleanupUsesConfiguredConfigurationTtlInsteadOfHardCodedThirtyDays() throws Exception {
        String source = Files.readString(Path.of("src/main/java/com/wolfe/experience/AnonymousConfigurationCleanupScheduler.java"));
        assertTrue(source.contains("WOLFE_CONFIGURATION_TTL_DAYS:30"));
        assertTrue(source.contains("Duration.ofDays(configurationTtlDays)"));
        assertTrue(!source.contains("Duration.ofDays(30)"));
    }
}

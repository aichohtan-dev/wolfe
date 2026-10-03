package com.wolfe.ops;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchedulerLockService {
    private final JdbcTemplate jdbc;
    public SchedulerLockService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Transactional
    public void runIfLeader(long lockKey, Runnable job) {
        Boolean acquired = jdbc.queryForObject("select pg_try_advisory_xact_lock(?)", Boolean.class, lockKey);
        if (Boolean.TRUE.equals(acquired)) job.run();
    }

    @Transactional
    public void lockForKey(String namespace, String value) {
        if (namespace == null || namespace.isBlank() || value == null || value.isBlank()) {
            throw new IllegalArgumentException("advisory lock namespace and value are required");
        }
        jdbc.queryForObject("select pg_advisory_xact_lock(hashtextextended(?, 0))", Long.class, namespace + ":" + value);
    }
}

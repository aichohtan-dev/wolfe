package com.wolfe.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {
    private static final long WINDOW_SECONDS = 15 * 60;
    private static final long MAX_SUBJECT_IP_ATTEMPTS = 5;
    private static final long MAX_IP_ATTEMPTS = 30;
    private static final long MAX_SUBJECT_ATTEMPTS = 20;
    private static final long MAX_GLOBAL_ATTEMPTS = 1000;
    private static final DefaultRedisScript<Long> SLIDING_WINDOW = new DefaultRedisScript<>(
            "local now = tonumber(ARGV[1]); local window = tonumber(ARGV[2]); local member = ARGV[3]; " +
            "redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, now - window); " +
            "redis.call('ZADD', KEYS[1], now, member); " +
            "redis.call('EXPIRE', KEYS[1], math.ceil(window / 1000) + 1); " +
            "return redis.call('ZCARD', KEYS[1]);", Long.class);
    private final StringRedisTemplate redis;
    public RateLimitService(StringRedisTemplate redis) {
        this.redis = redis;
    }
    public void check(String action, String email, String remoteAddress) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        String ip = normalizeIp(remoteAddress);
        String subjectHash = sha256(normalizedEmail);
        String ipHash = sha256(ip);
        checkKey("auth:rate:" + action + ":subject-ip:" + subjectHash + ":" + ipHash, MAX_SUBJECT_IP_ATTEMPTS);
        // Do not hard-block on an email-only bucket: that would let any remote attacker
        // deny a victim account. Keep the counter for telemetry/future step-up controls.
        checkKey("auth:rate:" + action + ":subject:" + subjectHash, MAX_SUBJECT_ATTEMPTS, false);
        checkKey("auth:rate:" + action + ":ip:" + ipHash, MAX_IP_ATTEMPTS);
        // Distributed global ceiling prevents a coordinated multi-IP attack from
        // bypassing the per-IP buckets while retaining generous headroom for normal traffic.
        checkKey("auth:rate:" + action + ":global", MAX_GLOBAL_ATTEMPTS);
    }
    public void clear(String action, String email, String remoteAddress) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        String ip = normalizeIp(remoteAddress);
        String subjectHash = sha256(normalizedEmail);
        String ipHash = sha256(ip);
        // Never clear the shared IP bucket after a successful login/register: that
        // would let an attacker reset the site-wide protection using their own account.
        redis.delete("auth:rate:" + action + ":subject-ip:" + subjectHash + ":" + ipHash);
    }
    /** Normalize the server-observed address before hashing it into a rate-limit key. */
    private static String normalizeIp(String value) {
        if (value == null || value.isBlank()) return "unknown";
        String ip = value.trim();
        if (ip.length() > 255) return "invalid";
        return ip;
    }

    private void checkKey(String key, long maxAttempts) { checkKey(key, maxAttempts, true); }

    private void checkKey(String key, long maxAttempts, boolean enforce) {
        final Long count;
        try {
            count = redis.execute(SLIDING_WINDOW, List.of(key), String.valueOf(System.currentTimeMillis()), String.valueOf(WINDOW_SECONDS * 1000L), UUID.randomUUID().toString());
        } catch (org.springframework.data.redis.RedisConnectionFailureException | org.springframework.data.redis.RedisSystemException ex) {
            throw new RateLimitUnavailableException();
        }
        if (enforce && count != null && count > maxAttempts) throw new RateLimitExceededException();
    }
    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash rate-limit key", e);
        }
    }
    public static class RateLimitExceededException extends RuntimeException { }
    public static class RateLimitUnavailableException extends RuntimeException { }
}

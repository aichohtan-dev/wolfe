package com.wolfe.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {
    private static final long WINDOW_SECONDS = 15 * 60;
    private static final long MAX_ATTEMPTS = 5;
    private final StringRedisTemplate redis;
    public RateLimitService(StringRedisTemplate redis) {
        this.redis = redis;
    }
    public void check(String action, String email, String remoteAddress) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        String ip = remoteAddress == null || remoteAddress.isBlank() ? "unknown" : remoteAddress;
        String emailHash = sha256(normalizedEmail);
        String ipHash = sha256(ip);
        checkKey("auth:rate:" + action + ":email:" + emailHash);
        checkKey("auth:rate:" + action + ":ip:" + ipHash);
    }
    public void clear(String action, String email, String remoteAddress) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        String ip = remoteAddress == null || remoteAddress.isBlank() ? "unknown" : remoteAddress;
        redis.delete("auth:rate:" + action + ":email:" + sha256(normalizedEmail));
        redis.delete("auth:rate:" + action + ":ip:" + sha256(ip));
    }
    private void checkKey(String key) {
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1L) redis.expire(key, Duration.ofSeconds(WINDOW_SECONDS));
        if (count != null && count > MAX_ATTEMPTS) {
            throw new RateLimitExceededException();
        }
    }
    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash rate-limit key", e);
        }
    }
    public static class RateLimitExceededException extends RuntimeException {
    }
}

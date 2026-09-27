package com.wolfe.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

@Service
public class JwtService {
    private final SecretKey key;
    private final long ttlSeconds;
    public JwtService(@Value("${WOLFE_JWT_SECRET:}") String secret, @Value("${WOLFE_JWT_TTL_SECONDS:900}") long ttlSeconds) {
        if (secret.length() < 32) throw new IllegalStateException("WOLFE_JWT_SECRET must be at least 32 characters and must be configured");
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttlSeconds = ttlSeconds;
    }
    public String issue(Long id, String email, String role) {
        Instant now = Instant.now();
        return Jwts.builder().subject(email).claim("customerId", id).claim("role", role)
        .issuedAt(Date.from(now)).expiration(Date.from(now.plus(Duration.ofSeconds(ttlSeconds)))).signWith(key).compact();
    }
    public Jws<Claims> parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    }
}

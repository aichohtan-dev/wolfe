package com.wolfe.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;
import com.wolfe.config.WolfeSecurityProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

@Service
public class JwtService {
    private final Map<String, SecretKey> keys;
    private final long ttlSeconds = 900;
    private final String issuer;
    private final String audience;
    private final String primaryKid;

    public JwtService(WolfeSecurityProperties config) {
        this.keys = loadKeys(config.jwtSecret(), config.jwtKid(), config.jwtKeys());
        this.primaryKid = config.jwtKid();
        this.issuer = config.jwtIssuer();
        this.audience = config.jwtAudience();
    }

    public String issue(Long id, String email, String role, long sessionVersion) {
        SecretKey key=keys.get(primaryKid);
        if(key==null) throw new IllegalStateException("WOLFE_JWT_KID must exist in configured JWT keys");
        Instant now=Instant.now();
        return Jwts.builder().issuer(issuer).audience().add(audience).and().header().keyId(primaryKid).and().subject(email)
            .claim("customerId",id).claim("sessionVersion",sessionVersion).issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(Duration.ofSeconds(ttlSeconds)))).signWith(key).compact();
    }

    public Jws<Claims> parse(String token) {
        JwtException last=null;
        for (var entry: keys.entrySet()) {
            try {
                Jws<Claims> parsed=Jwts.parser().verifyWith(entry.getValue()).requireIssuer(issuer).requireAudience(audience).build().parseSignedClaims(token);
                String kid=parsed.getHeader().getKeyId();
                if(kid==null || !entry.getKey().equals(kid)) throw new JwtException("invalid kid");
                return parsed;
            } catch (JwtException ex) { last=ex; }
        }
        throw last == null ? new JwtException("invalid token") : last;
    }

    private static Map<String,SecretKey> loadKeys(String secret,String defaultKid,String configured) {
        LinkedHashMap<String,SecretKey> out=new LinkedHashMap<>();
        if(configured!=null && !configured.isBlank()) {
            for(String entry:configured.split(",")) {
                String[] parts=entry.trim().split("=",2);
                if(parts.length!=2) throw new IllegalStateException("WOLFE_JWT_KEYS format is kid=secret,kid2=secret");
                put(out,parts[0].trim(),parts[1].trim());
            }
        } else {
            put(out,defaultKid,secret);
        }
        if(!out.containsKey(defaultKid)) throw new IllegalStateException("WOLFE_JWT_KID must exist in WOLFE_JWT_KEYS");
        return Collections.unmodifiableMap(out);
    }

    private static void put(Map<String,SecretKey> out,String kid,String secret) {
        if(kid.isBlank() || secret.length()<32 || secret.matches("(?i).*(change[_-]?me|replace|example|default|your[-_ ]secret).*"))
            throw new IllegalStateException("Each JWT key must have a real random secret of at least 32 characters");
        out.put(kid,Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)));
    }
}

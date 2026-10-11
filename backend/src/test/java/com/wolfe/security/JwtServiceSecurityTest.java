package com.wolfe.security;

import com.wolfe.config.WolfeSecurityProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceSecurityTest {
    private static JwtService service(String secret) {
        return new JwtService(new WolfeSecurityProperties(
                secret, "wolfe-api", "wolfe-web", "primary", "",
                "http://localhost:5173", false));
    }

    @Test
    void malformedTokenIsRejectedAsJwtException() {
        assertThrows(io.jsonwebtoken.JwtException.class, () -> service("abcdefghijklmnopqrstuvwxyz123456").parse("not-a-jwt"));
    }

    @Test
    void wrongSignatureIsRejectedAsJwtException() {
        JwtService issuer = service("abcdefghijklmnopqrstuvwxyz123456");
        String token = issuer.issue(7L, "a@example.com", "CUSTOMER", 0L);
        JwtService other = service("ZYXWVUTSRQPONMLKJIHGFEDCBA654321");
        assertThrows(io.jsonwebtoken.JwtException.class, () -> other.parse(token));
    }
}

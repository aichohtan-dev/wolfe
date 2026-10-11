package com.wolfe.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceSecurityTest {
    private JwtService service() {
        return new JwtService(
            "abcdefghijklmnopqrstuvwxyz123456",
            900L, "wolfe-api", "wolfe-web", "primary", ""
        );
    }

    @Test void malformedTokenIsRejectedAsJwtException() {
        assertThrows(io.jsonwebtoken.JwtException.class, () -> service().parse("not-a-jwt"));
    }

    @Test void wrongSignatureIsRejectedAsJwtException() {
        JwtService issuer = new JwtService("abcdefghijklmnopqrstuvwxyz123456", 900L, "wolfe-api", "wolfe-web", "primary", "");
        String token = issuer.issue(7L, "a@example.com", "CUSTOMER", 0L);
        JwtService other = new JwtService("ZYXWVUTSRQPONMLKJIHGFEDCBA654321", 900L, "wolfe-api", "wolfe-web", "primary", "");
        assertThrows(io.jsonwebtoken.JwtException.class, () -> other.parse(token));
    }
}

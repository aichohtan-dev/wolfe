package com.wolfe.security;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import static org.mockito.Mockito.*;

class RateLimitServiceContractTest {
    @Test void usesServerObservedAddressAndDoesNotReadForwardedHeaders() throws Exception {
        // Contract-level guard: CustomerController obtains the rate-limit address
        // from HttpServletRequest.getRemoteAddr(), not X-Forwarded-For.
        var source = new String(java.nio.file.Files.readAllBytes(
            java.nio.file.Paths.get("src/main/java/com/wolfe/customer/CustomerController.java")
        ));
        org.junit.jupiter.api.Assertions.assertTrue(source.contains("request.getRemoteAddr()"));
        org.junit.jupiter.api.Assertions.assertFalse(source.contains("getHeader("X-Forwarded-For")"));
    }
}

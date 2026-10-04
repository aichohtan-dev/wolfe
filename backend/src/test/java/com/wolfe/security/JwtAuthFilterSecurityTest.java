package com.wolfe.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class JwtAuthFilterSecurityTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void invalidAccessCookieDoesNotBlockFilterChainOrCreateAuthentication() throws Exception {
        var jwt = mock(JwtService.class);
        var customers = mock(com.wolfe.customer.CustomerRepository.class);
        var filter = new JwtAuthFilter(jwt, customers);
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var chain = mock(FilterChain.class);

        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(new Cookie[] {
            new Cookie("wolfe_access", "expired-or-invalid")
        });
        when(jwt.parse("expired-or-invalid")).thenThrow(new JwtException("invalid"));

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(customers);
        org.junit.jupiter.api.Assertions.assertNull(
            SecurityContextHolder.getContext().getAuthentication()
        );
    }
}

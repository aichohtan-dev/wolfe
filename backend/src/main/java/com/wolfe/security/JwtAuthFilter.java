package com.wolfe.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import com.wolfe.customer.CustomerRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.Claims;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final CustomerRepository customers;
    public JwtAuthFilter(JwtService jwt, CustomerRepository customers) {
        this.jwt = jwt;
        this.customers = customers;
    }
    @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        String h = req.getHeader("Authorization");
        String token = h != null && h.startsWith("Bearer ") ? h.substring(7) : firstCookie(req, "__Host-wolfe_access", "wolfe_access");
        if (token != null && !token.isBlank()) {
            try {
                var c = jwt.parse(token).getPayload();
                Object rawId = c.get("customerId");
                if (rawId == null) throw new IllegalArgumentException("missing_customer_id");
                Long id = rawId instanceof Number n?n.longValue():Long.valueOf(rawId.toString());
                var customer = customers.findById(id).orElseThrow(() -> new IllegalArgumentException("customer_not_found"));
                Object rawVersion = c.get("sessionVersion");
                long tokenVersion = rawVersion == null ? 0L : (rawVersion instanceof Number n ? n.longValue() : Long.parseLong(String.valueOf(rawVersion)));
                if (tokenVersion != customer.getSessionVersion()) throw new IllegalArgumentException("session_revoked");
                if (!customer.isEnabled() || customer.isLocked()) throw new IllegalArgumentException("customer_disabled_or_locked");
                String role = customer.getRole();
                if (role == null || role.isBlank()) throw new IllegalArgumentException("invalid_customer_role");
                var authorities = new ArrayList<SimpleGrantedAuthority>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                RolePermissions.forRole(role).forEach(permission -> authorities.add(new SimpleGrantedAuthority("PERM_" + permission)));
                var auth = new UsernamePasswordAuthenticationToken(customer.getEmail(), null, authorities);
                auth.setDetails(id);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception ignored) {
                // Do not turn a stale optional cookie into a global 401. Clearing the
                // authentication context lets Spring Security's authorization rules
                // decide: permitAll routes continue anonymously; protected routes are
                // rejected by the configured AuthenticationEntryPoint.
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(req, res);
    }
    private String firstCookie(HttpServletRequest req, String... names) {
        if (req.getCookies() == null) return null;
        for (jakarta.servlet.http.Cookie c : req.getCookies()) for (String name : names) if (name.equals(c.getName())) return c.getValue();
        return null;
    }
}

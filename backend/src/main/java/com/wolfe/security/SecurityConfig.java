package com.wolfe.security;

import java.util.List;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

@Configuration
public class SecurityConfig {
    @Bean BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
    @Bean SecurityFilterChain security(HttpSecurity http, JwtAuthFilter jwt) throws Exception {
        http.csrf(c -> c.disable()).headers(h -> h.contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; img-src 'self' data: https:; style-src 'self' 'unsafe-inline'; script-src 'self'; connect-src 'self' http://localhost:5173 http://localhost:8080; font-src 'self' data:; frame-ancestors 'none'")).frameOptions(f -> f.deny()).referrerPolicy(r -> r.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))).cors(c -> c.configurationSource(cors())).sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(a -> a.requestMatchers(
            "/api/v1/customers/register", "/api/v1/customers/login", "/api/v1/customers/refresh", "/api/v1/customers/logout",
            "/api/v1/products", "/api/v1/products/**",
            "/api/v1/bundles", "/api/v1/bundles/**",
            "/api/v1/visual-content", "/api/v1/visual-content/**",
            "/api/v1/experience/products/**", "/api/v1/experience/visual/**",
            "/api/v1/experience/configurations", "/api/v1/experience/configurations/**",
            "/api/v1/orders/shipping-quote", "/api/v1/orders/coupon-quote",
            "/api/v1/reviews/product/**",
            "/actuator/health", "/actuator/health/**", "/swagger-ui/**", "/v3/api-docs/**"
        ).permitAll()
        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN").anyRequest().authenticated())
        .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    private CorsConfigurationSource cors() {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOrigins(List.of(System.getenv().getOrDefault("WOLFE_FRONTEND_ORIGIN", "http://localhost:5173")));
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource();
        s.registerCorsConfiguration("/**", c);
        return s;
    }
}

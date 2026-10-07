package com.wolfe.security;

import java.util.List;
import org.springframework.context.annotation.*;
import com.wolfe.config.WolfeSecurityProperties;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.http.MediaType;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.*;

@Configuration
public class SecurityConfig {
    @Bean BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean SecurityFilterChain security(HttpSecurity http, JwtAuthFilter jwt, SecurityEventLogger events,
                                       CorsConfigurationSource cors) throws Exception {
        http.csrf(c -> c.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
        .exceptionHandling(e -> e
            .authenticationEntryPoint((req,res,ex) -> { res.setStatus(401); res.setContentType(MediaType.APPLICATION_JSON_VALUE); events.denied(req,401,"authentication_required"); res.getWriter().write("{\"error\":\"UNAUTHORIZED\",\"message\":\"Authentication required\"}"); })
            .accessDeniedHandler((req,res,ex) -> { res.setStatus(403); res.setContentType(MediaType.APPLICATION_JSON_VALUE); events.denied(req,403,"authorization_denied"); res.getWriter().write("{\"error\":\"FORBIDDEN\",\"message\":\"Access denied\"}"); }))
        .headers(h -> h.contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; img-src 'self' data: https:; style-src 'self'; style-src-attr 'unsafe-inline'; style-src-elem 'self'; script-src 'self' https://challenges.cloudflare.com; connect-src 'self' https://challenges.cloudflare.com; font-src 'self' data:; object-src 'none'; frame-src https://challenges.cloudflare.com; frame-ancestors 'none'; base-uri 'self'; form-action 'self'"))
            .frameOptions(f -> f.deny())
            .addHeaderWriter(new StaticHeadersWriter("Cross-Origin-Opener-Policy", "same-origin"))
            .addHeaderWriter(new StaticHeadersWriter("Cross-Origin-Resource-Policy", "same-origin"))
            .referrerPolicy(r -> r.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)))
        .cors(c -> c.configurationSource(cors))
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(a -> a.requestMatchers(
            "/error", "/api/v1/customers/register", "/api/v1/customers/login", "/api/v1/customers/refresh", "/api/v1/customers/logout",
            "/api/v1/customers/csrf", "/catalog/published/**", "/api/v1/consultations",
            "/api/v1/products", "/api/v1/products/**", "/api/v1/brands", "/api/v1/brands/**",
            "/api/v1/subcategories", "/api/v1/subcategories/**", "/api/v1/bundles", "/api/v1/bundles/**",
            "/api/v1/visual-content", "/api/v1/visual-content/**", "/api/v1/experience/products/**",
            "/api/v1/experience/visual/**", "/api/v1/experience/configurations", "/api/v1/experience/configurations/**",
            "/api/v1/orders/shipping-quote", "/api/v1/orders/coupon-quote", "/actuator/health", "/actuator/health/**",
            "/sitemap.xml", "/robots.txt"
        ).permitAll()
        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/reviews/product/**").permitAll()
        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/reviews/product/**").authenticated()
        .requestMatchers("/api/v1/retailer/**").hasRole("RETAILER")
        .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
        .requestMatchers("/api/v1/consultations/admin", "/api/v1/consultations/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
        .requestMatchers("/actuator/info").hasAnyRole("ADMIN", "SUPER_ADMIN")
        .anyRequest().authenticated())
        .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean CorsConfigurationSource corsConfigurationSource(WolfeSecurityProperties config) {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOrigins(java.util.Arrays.stream(config.frontendOrigins().split(","))
            .map(String::trim).filter(v -> !v.isBlank()).distinct().toList());
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        c.setAllowCredentials(true);
        c.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-XSRF-TOKEN"));
        UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource();
        s.registerCorsConfiguration("/**", c);
        return s;
    }
}

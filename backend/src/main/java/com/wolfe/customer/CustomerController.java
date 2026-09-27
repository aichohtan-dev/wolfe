package com.wolfe.customer;

import com.wolfe.security.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
    private final CustomerRepository repo;
    private final BCryptPasswordEncoder encoder;
    private final SessionService sessions;
    private final com.wolfe.security.RateLimitService rateLimits;
    public CustomerController(CustomerRepository repo, BCryptPasswordEncoder encoder, SessionService sessions, com.wolfe.security.RateLimitService rateLimits) {
        this.repo = repo;
        this.encoder = encoder;
        this.sessions = sessions;
        this.rateLimits = rateLimits;
    }
    public record RegisterRequest(@NotBlank String name, @Email @NotBlank String email, @Size(min = 8, max = 128) String password) {
    }
    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {
    }
    public record ProfileRequest(@NotBlank String name, @Size(max = 30) String phone) {
    }
    private Map<String, Object> session(Customer c) {
        var s = sessions.issue(c);
        return Map.of("accessToken", s.accessToken(), "refreshToken", s.refreshToken(), "customer", Map.of("id", c.getId(), "name", c.getName(), "email",
        c.getEmail(), "phone", c.getPhone() == null?"":c.getPhone(), "role",
        c.getRole()));
    }
    @PostMapping("/register") ResponseEntity<?> register(@Valid @RequestBody RegisterRequest r, HttpServletRequest request) {
        rateLimits.check("register", r.email(), request.getRemoteAddr());
        if (repo.findByEmailIgnoreCase(r.email()).isPresent())return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "EMAIL_EXISTS"));
        Customer c = repo.save(new Customer(r.name().trim(), r.email().trim().toLowerCase(), encoder.encode(r.password())));
        rateLimits.clear("register", r.email(), request.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(session(c));
    }
    @PostMapping("/login") ResponseEntity<?> login(@Valid @RequestBody LoginRequest r, HttpServletRequest request) {
        rateLimits.check("login", r.email(), request.getRemoteAddr());
        return repo.findByEmailIgnoreCase(r.email().trim().toLowerCase()).filter(c -> encoder.matches(r.password(),
        c.getPasswordHash())).<ResponseEntity<?>>map(c -> {
            rateLimits.clear("login", r.email(), request.getRemoteAddr()); return ResponseEntity.ok(session(c));
        }
        ).orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "INVALID_CREDENTIALS")));
    }
    @PostMapping("/refresh") ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        try {
            var s = sessions.rotate(body.getOrDefault("refreshToken", ""));
            return ResponseEntity.ok(Map.of("accessToken", s.accessToken(), "refreshToken", s.refreshToken()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "INVALID_REFRESH_TOKEN"));
        }
    }
    @PostMapping("/logout") ResponseEntity<?> logout(@RequestBody Map<String, String> body) {
        sessions.revoke(body.getOrDefault("refreshToken", ""));
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/me") Map<String, Object> me(Authentication auth) {
        Long id = (Long)auth.getDetails();
        var c = repo.findById(id).orElseThrow();
        return profile(c);
    }
    @PutMapping("/me") Map<String, Object> update(@Valid @RequestBody ProfileRequest r, Authentication auth) {
        Long id = (Long)auth.getDetails();
        var c = repo.findById(id).orElseThrow();
        c.updateProfile(r.name(), r.phone());
        return profile(repo.save(c));
    }
    private Map<String, Object> profile(Customer c) {
        return Map.of("id", c.getId(), "name", c.getName(), "email", c.getEmail(), "phone", c.getPhone() == null?"":c.getPhone());
    }
}

package com.wolfe.customer;

import com.wolfe.security.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import org.springframework.http.*;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);
    private static final String DUMMY_BCRYPT_HASH = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
    private final CustomerRepository repo;
    private final BCryptPasswordEncoder encoder;
    private final SessionService sessions;
    private final AccountLifecycleService lifecycle;
    private final com.wolfe.security.SecurityEventLogger securityEvents;
    private final com.wolfe.security.RateLimitService rateLimits;
    private final boolean secureCookies;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;
    private final String sameSite;
    public CustomerController(CustomerRepository repo, BCryptPasswordEncoder encoder, SessionService sessions, AccountLifecycleService lifecycle, com.wolfe.security.RateLimitService rateLimits,
                               @Value("${WOLFE_SECURE_COOKIES:true}") boolean secureCookies, @Value("${WOLFE_JWT_TTL_SECONDS:900}") long accessTtlSeconds,
                               @Value("${WOLFE_REFRESH_TTL_SECONDS:2592000}") long refreshTtlSeconds,
                               @Value("${WOLFE_COOKIE_SAMESITE:Lax}") String sameSite, com.wolfe.security.SecurityEventLogger securityEvents) {
        this.repo = repo; this.encoder = encoder; this.sessions = sessions; this.lifecycle = lifecycle; this.rateLimits = rateLimits;
        this.secureCookies = secureCookies; this.accessTtlSeconds = accessTtlSeconds; this.refreshTtlSeconds = refreshTtlSeconds;
        this.sameSite = validateSameSite(sameSite);
        if ("None".equals(this.sameSite) && !secureCookies) throw new IllegalStateException("WOLFE_COOKIE_SAMESITE=None requires WOLFE_SECURE_COOKIES=true"); this.securityEvents=securityEvents;
    }
    public record RegisterRequest(@NotBlank @Size(max = 100) String name, @Email @NotBlank @Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[A-Za-z]{2,63}$") @Size(max = 254) String email, @NotBlank @Size(min = 8, max = 72) String password) {
    }
    public record LoginRequest(@Email @NotBlank @Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[A-Za-z]{2,63}$") @Size(max = 254) String email, @NotBlank @Size(max = 72) String password) {
    }
    public record ProfileRequest(@NotBlank @Size(max = 100) String name, @Pattern(regexp = "^$|^\\+?[1-9]\\d{7,14}$") @Size(max = 16) String phone) {
    }
    private ResponseEntity<Map<String, Object>> session(Customer c, HttpServletRequest request) {
        String device = request.getHeader("User-Agent");
        if (device != null && device.length() > 120) device = device.substring(0,120);
        var s = sessions.issue(c, device);
        var body = Map.<String,Object>of("customer", Map.of("id", c.getId(), "name", c.getName(), "email",
        c.getEmail(), "phone", c.getPhone() == null?"":c.getPhone(), "emailVerified", c.isEmailVerified(), "role", c.getRole()));
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, accessCookie(s.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie(s.refreshToken()).toString()).body(body);
    }
    private ResponseCookie accessCookie(String token) {
        return ResponseCookie.from(accessCookieName(), token).httpOnly(true).secure(secureCookies).sameSite(sameSite).path("/").maxAge(accessTtlSeconds).build();
    }
    private ResponseCookie refreshCookie(String token) {
        return ResponseCookie.from(refreshCookieName(), token).httpOnly(true).secure(secureCookies).sameSite(sameSite).path("/api/v1/customers").maxAge(refreshTtlSeconds).build();
    }
    private String accessCookieName() { return secureCookies ? "__Host-wolfe_access" : "wolfe_access"; }
    private String refreshCookieName() { return secureCookies ? "__Secure-wolfe_refresh" : "wolfe_refresh"; }
    private ResponseCookie clearCookie(String name, String path) {
        return ResponseCookie.from(name, "").httpOnly(true).secure(secureCookies).sameSite(sameSite).path(path).maxAge(0).build();
    }
    @GetMapping("/csrf") Map<String, String> csrf(CsrfToken token) {
        return Map.of("token", token.getToken());
    }
    @PostMapping("/register") ResponseEntity<?> register(@Valid @RequestBody RegisterRequest r, HttpServletRequest request) {
        validatePasswordBytes(r.password());
        rateLimits.check("register", r.email(), clientIp(request));
        String normalizedEmail = r.email().trim().toLowerCase();
        Optional<Customer> existing = repo.findByEmailIgnoreCase(normalizedEmail);

        // Do equivalent password work for duplicate emails so timing does not become an
        // account-existence oracle. Do not clear the rate limit on duplicate probes.
        String encodedPassword = encoder.encode(r.password());
        if (existing.isPresent()) {
            return ResponseEntity.accepted().body(Map.of(
                    "message", "If registration is available for the submitted details, the account is now available. Please sign in."
            ));
        }

        Customer c = repo.save(new Customer(r.name().trim(), normalizedEmail, encodedPassword));
        c.markEmailUnverified();
        repo.save(c);
        lifecycle.sendVerification(c.getId());
        rateLimits.clear("register", normalizedEmail, clientIp(request));
        return ResponseEntity.accepted().body(Map.of(
                "message", "If registration is available for the submitted details, the account is now available. Please sign in."
        ));
    }
    @PostMapping("/login") ResponseEntity<?> login(@Valid @RequestBody LoginRequest r, HttpServletRequest request) {
        String clientIp = clientIp(request);
        validatePasswordBytes(r.password());
        rateLimits.check("login", r.email(), clientIp);
        Optional<Customer> candidate = repo.findByEmailIgnoreCase(r.email().trim().toLowerCase());
        // Always perform BCrypt work, including for unknown/disabled/locked accounts,
        // so login timing does not reveal account existence/state.
        String hash = candidate.filter(c -> c.isEnabled() && !c.isLocked())
                .map(Customer::getPasswordHash).orElse(DUMMY_BCRYPT_HASH);
        boolean passwordOk = encoder.matches(r.password(), hash);
        return candidate.filter(c -> c.isEnabled() && !c.isLocked() && passwordOk).<ResponseEntity<?>>map(c -> {
            rateLimits.clear("login", r.email(), clientIp(request));
            securityEvents.loginSuccess(c.getId(), request);
            return session(c, request);
        }).orElseGet(() -> {
            log.warn("Failed login attempt for subject hash={} from {}", Integer.toHexString(r.email().trim().toLowerCase().hashCode()), clientIp);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "INVALID_CREDENTIALS", "message", "Invalid email or password"));
        });
    }
    @PostMapping("/refresh") ResponseEntity<?> refresh(HttpServletRequest request) {
        try {
            String raw = cookie(request, refreshCookieName());
            var s = sessions.rotate(raw);
            securityEvents.refreshSuccess(request);
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, accessCookie(s.accessToken()).toString())
                    .header(HttpHeaders.SET_COOKIE, refreshCookie(s.refreshToken()).toString()).build();
        } catch (SessionService.InvalidRefreshTokenReuseException e) {
            securityEvents.refreshReuse(e.getCustomerId(), request);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "INVALID_REFRESH_TOKEN", "message", "Refresh session is invalid or expired"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "INVALID_REFRESH_TOKEN", "message", "Refresh session is invalid or expired"));
        }
    }
    @PostMapping("/logout") ResponseEntity<?> logout(Authentication auth, jakarta.servlet.http.HttpServletRequest request) {
        if (auth != null && auth.getDetails() instanceof Long id) { repo.findByIdForUpdate(id).ifPresent(c -> { c.incrementSessionVersion(); repo.save(c); sessions.revokeAll(id); securityEvents.logout(id, request); }); }
        else { sessions.revoke(cookie(request, refreshCookieName())); securityEvents.logout(null, request); }
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, clearCookie(accessCookieName(), "/").toString())
                .header(HttpHeaders.SET_COOKIE, clearCookie(refreshCookieName(), "/api/v1/customers").toString()).build();
    }
    public record PasswordResetRequest(@Email @NotBlank @Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[A-Za-z]{2,63}$") @Size(max=254) String email) {}
    public record PasswordResetConfirm(@NotBlank String token, @NotBlank @Size(min=8,max=72) String newPassword) {}
    @PostMapping("/verify-email/request") ResponseEntity<?> requestEmailVerification(@Valid @RequestBody PasswordResetRequest r, HttpServletRequest request) { rateLimits.check("verify-email", r.email(), clientIp(request)); lifecycle.sendVerification(repo.findByEmailIgnoreCase(r.email().trim().toLowerCase()).map(Customer::getId).orElse(null)); return ResponseEntity.accepted().body(Map.of("message","If the account exists, a verification message has been sent.")); }
    @PostMapping("/verify-email/confirm") ResponseEntity<?> confirmEmail(@RequestParam String token) { return lifecycle.verifyEmail(token) ? ResponseEntity.noContent().build() : ResponseEntity.badRequest().body(Map.of("error","INVALID_TOKEN","message","Verification link is invalid or expired.")); }
    @PostMapping("/password-reset/request") ResponseEntity<?> requestReset(@Valid @RequestBody PasswordResetRequest r, HttpServletRequest request) { rateLimits.check("password-reset", r.email(), clientIp(request)); lifecycle.requestPasswordReset(r.email()); return ResponseEntity.accepted().body(Map.of("message","If the account exists, a password reset message has been sent.")); }
    @PostMapping("/password-reset/confirm") ResponseEntity<?> confirmReset(@Valid @RequestBody PasswordResetConfirm r, HttpServletRequest request) { rateLimits.check("password-reset-confirm", clientIp(request), clientIp(request)); validatePasswordBytes(r.newPassword()); return lifecycle.resetPassword(r.token(), encoder.encode(r.newPassword())) ? ResponseEntity.noContent().build() : ResponseEntity.badRequest().body(Map.of("error","INVALID_TOKEN","message","Reset link is invalid or expired.")); }
    @GetMapping("/sessions") java.util.List<SessionService.SessionInfo> sessions(Authentication auth) { return sessions.listActive((Long) auth.getDetails()); }
    @DeleteMapping("/sessions/{sessionId}") ResponseEntity<?> revokeSession(@PathVariable Long sessionId, Authentication auth) { sessions.revokeOne((Long) auth.getDetails(), sessionId); return ResponseEntity.noContent().build(); }

    private static String validateSameSite(String value) { String normalized=value==null?"Lax":value.trim(); if(!Set.of("Lax","Strict","None").contains(normalized)) throw new IllegalStateException("WOLFE_COOKIE_SAMESITE must be Lax, Strict or None"); return normalized; }

    private static void validatePasswordBytes(String password) {
        if (password != null && password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("password must be at most 72 UTF-8 bytes");
        }
    }

    private String cookie(jakarta.servlet.http.HttpServletRequest request, String name) {
        if (request.getCookies() == null) return "";
        for (jakarta.servlet.http.Cookie c : request.getCookies()) if (name.equals(c.getName())) return c.getValue();
        return "";
    }
    public record ChangePasswordRequest(@NotBlank String currentPassword, @NotBlank @Size(min = 8, max = 72) String newPassword) {}

    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/password")
    ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest r, Authentication auth) {
        Long id = (Long) auth.getDetails();
        validatePasswordBytes(r.newPassword());
        Customer c = repo.findByIdForUpdate(id).orElseThrow(() -> new NoSuchElementException("Customer not found"));
        if (!encoder.matches(r.currentPassword(), c.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "INVALID_CREDENTIALS", "message", "Current password is incorrect"));
        }
        c.changePasswordHash(encoder.encode(r.newPassword()));
        c.incrementSessionVersion();
        repo.save(c);
        sessions.revokeAll(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    ResponseEntity<?> deleteAccount(Authentication auth) {
        Long id = (Long) auth.getDetails();
        Customer c = repo.findByIdForUpdate(id).orElseThrow(() -> new NoSuchElementException("Customer not found"));
        c.setEnabled(false); c.setLocked(true); c.incrementSessionVersion();
        c.anonymize();
        repo.save(c);
        sessions.revokeAll(id);
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, clearCookie(accessCookieName(), "/").toString())
                .header(HttpHeaders.SET_COOKIE, clearCookie(refreshCookieName(), "/api/v1/customers").toString()).build();
    }

    @GetMapping("/me") Map<String, Object> me(Authentication auth) {
        Long id = (Long)auth.getDetails();
        var c = repo.findById(id).orElseThrow(() -> new NoSuchElementException("Customer not found"));
        return profile(c);
    }
    @PutMapping("/me") Map<String, Object> update(@Valid @RequestBody ProfileRequest r, Authentication auth) {
        Long id = (Long)auth.getDetails();
        var c = repo.findByIdForUpdate(id).orElseThrow(() -> new NoSuchElementException("Customer not found"));
        c.updateProfile(r.name(), r.phone());
        return profile(repo.save(c));
    }
    /** The API is private behind the trusted Nginx proxy; do not trust client-supplied forwarding headers. */
    private String clientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    private Map<String, Object> profile(Customer c) {
        return Map.of("id", c.getId(), "name", c.getName(), "email", c.getEmail(), "phone", c.getPhone() == null?"":c.getPhone(), "emailVerified", c.isEmailVerified());
    }
}

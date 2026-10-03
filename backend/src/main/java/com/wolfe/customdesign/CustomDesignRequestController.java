package com.wolfe.customdesign;

import com.wolfe.security.CustomerAccess;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/custom-design") public class CustomDesignRequestController {
    private final CustomDesignRequestRepository repo;
    private final com.wolfe.security.RateLimitService rateLimits;
    private final com.wolfe.admin.StaffAlertService alerts;
    private final com.wolfe.security.CaptchaService captcha;
    public CustomDesignRequestController(CustomDesignRequestRepository r, com.wolfe.security.RateLimitService rateLimits, com.wolfe.admin.StaffAlertService alerts, com.wolfe.security.CaptchaService captcha) {
        repo = r;
        this.rateLimits = rateLimits; this.alerts=alerts; this.captcha=captcha;
    }
    public record Create(@NotBlank @Size(max = 160) String projectName, @Size(max = 2000) String requirements, @Size(max = 2000) String referenceImageUrl, @Size(max=120) String website, @Size(max=4096) String captchaToken) {
    }
    @PostMapping("/customers/{customerId}") public CustomDesignRequest create(@PathVariable Long customerId, @Valid @RequestBody Create r, Authentication a, HttpServletRequest request) {
        CustomerAccess.requireCustomer(a, customerId);
        rateLimits.check("custom-design", String.valueOf(customerId), clientIp(request));
        if (r.website()!=null && !r.website().isBlank()) throw new IllegalArgumentException("Invalid custom-design request");
        if (!captcha.verify(r.captchaToken(), clientIp(request))) throw new IllegalArgumentException("Human verification failed");
        validateReferenceUrl(r.referenceImageUrl());
        var saved=repo.save(new CustomDesignRequest(customerId, r.projectName().trim(), r.requirements(), r.referenceImageUrl())); alerts.lead("CUSTOM_DESIGN", "New custom design request", "Customer "+customerId+" submitted a custom design request"); return saved;
    }
    private static void validateReferenceUrl(String value) {
        if (value == null || value.isBlank()) return;
        try {
            var u = java.net.URI.create(value.trim());
            if (!"https".equalsIgnoreCase(u.getScheme()) || u.getHost() == null) throw new IllegalArgumentException("referenceImageUrl must be HTTPS");
        } catch (IllegalArgumentException ex) { throw new IllegalArgumentException("invalid referenceImageUrl"); }
    }

    /** The API is private behind the trusted Nginx proxy; do not trust client-supplied forwarding headers. */
    private String clientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
    @GetMapping("/customers/{customerId}") public List<CustomDesignRequest> mine(@PathVariable Long customerId, Authentication a) {
        CustomerAccess.requireCustomer(a, customerId);
        return repo.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }
}

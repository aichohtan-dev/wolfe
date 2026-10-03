package com.wolfe.quote;

import com.wolfe.security.CustomerAccess;
import com.wolfe.experience.ProductConfiguration;
import com.wolfe.experience.ConfigurationRepository;
import com.wolfe.catalog.ProductRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/quotes") public class QuoteRequestController {
    private final QuoteRequestRepository repo;
    private final ConfigurationRepository configurations;
    private final com.wolfe.security.RateLimitService rateLimits;
    private final ProductRepository products;
    private final com.wolfe.admin.StaffAlertService alerts;
    private final com.wolfe.security.CaptchaService captcha;
    public QuoteRequestController(QuoteRequestRepository r, ConfigurationRepository configurations, com.wolfe.security.RateLimitService rateLimits, ProductRepository products, com.wolfe.admin.StaffAlertService alerts, com.wolfe.security.CaptchaService captcha) {
        repo = r;
        this.configurations = configurations;
        this.rateLimits = rateLimits;
        this.products = products;
        this.alerts = alerts; this.captcha=captcha;
    }
    public record Create(@NotBlank @Size(max = 1000) String message, Long productId, Long configurationId, @Size(max=120) String website, @Size(max=4096) String captchaToken) {
    }
    @PostMapping("/customers/{customerId}") public QuoteRequest create(@PathVariable Long customerId, @Valid @RequestBody Create r, Authentication a, HttpServletRequest request) {
        CustomerAccess.requireCustomer(a, customerId);
        rateLimits.check("quote", String.valueOf(customerId), clientIp(request));
        if (r.website()!=null && !r.website().isBlank()) throw new IllegalArgumentException("Invalid quote request");
        if (!captcha.verify(r.captchaToken(), clientIp(request))) throw new IllegalArgumentException("Human verification failed");
        if (r.productId() != null) {
            products.findById(r.productId()).filter(com.wolfe.catalog.Product::isActive)
                    .orElseThrow(() -> new IllegalArgumentException("product not found or inactive"));
        }
        if (r.configurationId() != null) {
            ProductConfiguration cfg = configurations.findById(r.configurationId())
                    .orElseThrow(() -> new IllegalArgumentException("configuration not found"));
            if (cfg.getCustomerId() != null && !cfg.getCustomerId().equals(customerId)) {
                throw new org.springframework.security.access.AccessDeniedException("Configuration access denied");
            }
            if (r.productId() != null && !r.productId().equals(cfg.getProductId())) {
                throw new IllegalArgumentException("configuration does not belong to product");
            }
        }
        var q = repo.save(new QuoteRequest(customerId, r.productId(), r.message().trim()));
        q.setConfigurationId(r.configurationId());
        var saved=repo.save(q); alerts.lead("QUOTE", "New quote request", "Customer "+customerId+" requested a quote"); return saved;
    }
    /** The API is private behind the trusted Nginx proxy; do not trust client-supplied forwarding headers. */
    private String clientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
    @GetMapping("/customers/{customerId}") public List<QuoteRequest> mine(@PathVariable Long customerId, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size, Authentication a) {
        CustomerAccess.requireCustomer(a, customerId);
        return repo.findByCustomerIdOrderByCreatedAtDesc(customerId, org.springframework.data.domain.PageRequest.of(Math.max(0,page),Math.min(Math.max(1,size),100))).getContent();
    }
}

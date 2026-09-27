package com.wolfe.quote;

import com.wolfe.security.CustomerAccess;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/quotes") public class QuoteRequestController {
    private final QuoteRequestRepository repo;
    public QuoteRequestController(QuoteRequestRepository r) {
        repo = r;
    }
    public record Create(@NotBlank @Size(max = 1000) String message, Long productId, Long configurationId) {
    }
    @PostMapping("/customers/{customerId}") public QuoteRequest create(@PathVariable Long customerId, @Valid @RequestBody Create r, Authentication a) {
        CustomerAccess.requireCustomer(a, customerId);
        var q = repo.save(new QuoteRequest(customerId, r.productId(), r.message().trim()));
        q.setConfigurationId(r.configurationId());
        return repo.save(q);
    }
    @GetMapping("/customers/{customerId}") public List<QuoteRequest> mine(@PathVariable Long customerId, Authentication a) {
        CustomerAccess.requireCustomer(a, customerId);
        return repo.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }
}

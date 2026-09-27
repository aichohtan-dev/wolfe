package com.wolfe.customdesign;

import com.wolfe.security.CustomerAccess;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/custom-design") public class CustomDesignRequestController {
    private final CustomDesignRequestRepository repo;
    public CustomDesignRequestController(CustomDesignRequestRepository r) {
        repo = r;
    }
    public record Create(@NotBlank @Size(max = 160) String projectName, @Size(max = 2000) String requirements, @Size(max = 2000) String referenceImageUrl) {
    }
    @PostMapping("/customers/{customerId}") public CustomDesignRequest create(@PathVariable Long customerId, @Valid @RequestBody Create r, Authentication a) {
        CustomerAccess.requireCustomer(a, customerId);
        return repo.save(new CustomDesignRequest(customerId, r.projectName().trim(), r.requirements(), r.referenceImageUrl()));
    }
    @GetMapping("/customers/{customerId}") public List<CustomDesignRequest> mine(@PathVariable Long customerId, Authentication a) {
        CustomerAccess.requireCustomer(a, customerId);
        return repo.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }
}

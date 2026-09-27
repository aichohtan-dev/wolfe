package com.wolfe.notification;

import java.util.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import static com.wolfe.security.CustomerAccess.requireCustomer;

@RestController
@RequestMapping("/api/v1/customers/{customerId}/notifications")
public class NotificationController {
    private final CustomerNotificationRepository repo;
    public NotificationController(CustomerNotificationRepository repo) {
        this.repo = repo;
    }
    @GetMapping public Map<String, Object> list(@PathVariable Long customerId, Authentication auth) {
        requireCustomer(auth, customerId);
        return Map.of("items", repo.findByCustomerIdOrderByCreatedAtDesc(customerId), "unread", repo.countByCustomerIdAndReadAtIsNull(customerId));
    }
    @PostMapping("/{id}/read") public CustomerNotification read(@PathVariable Long customerId, @PathVariable Long id, Authentication auth) {
        requireCustomer(auth, customerId);
        var n = repo.findById(id).orElseThrow(() -> new NoSuchElementException("Notification not found"));
        if (!n.getCustomerId().equals(customerId))throw new org.springframework.security.access.AccessDeniedException("Notification access denied");
        n.markRead();
        return repo.save(n);
    }
    @PostMapping("/read-all") public Map<String, Object> readAll(@PathVariable Long customerId, Authentication auth) {
        requireCustomer(auth, customerId);
        var rows = repo.findByCustomerIdOrderByCreatedAtDesc(customerId);
        rows.forEach(n -> {
            if (n.getReadAt() == null)n.markRead();
        }
        );
        repo.saveAll(rows);
        return Map.of("updated", rows.size());
    }
}

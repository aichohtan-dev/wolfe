package com.wolfe.consultation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/consultations")
public class ConsultationRequestController {
    private final ConsultationRequestRepository repo;
    private final com.wolfe.security.RateLimitService rateLimits;
    private final com.wolfe.admin.StaffAlertService alerts;
    private final com.wolfe.security.CaptchaService captcha;
    public ConsultationRequestController(ConsultationRequestRepository repo, com.wolfe.security.RateLimitService rateLimits, com.wolfe.admin.StaffAlertService alerts, com.wolfe.security.CaptchaService captcha){this.repo=repo;this.rateLimits=rateLimits;this.alerts=alerts; this.captcha=captcha;}
    public record Create(@NotBlank @Size(max=100) String name,@NotBlank @Email @Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[A-Za-z]{2,63}$") @Size(max=150) String email,@Size(max=160) String project,@NotBlank @Size(min=10,max=1000) String message,@Size(max=120) String website,@Size(max=4096) String captchaToken){}
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConsultationRequest create(@Valid @RequestBody Create r, HttpServletRequest request){
        rateLimits.check("consultation", r.email(), clientIp(request));
        if (r.website()!=null && !r.website().isBlank()) throw new IllegalArgumentException("Invalid consultation request");
        if (!captcha.verify(r.captchaToken(), clientIp(request))) throw new IllegalArgumentException("Human verification failed");
        var saved = repo.save(new ConsultationRequest(r.name().trim(),r.email().trim().toLowerCase(Locale.ROOT),r.project()==null?null:r.project().trim(),r.message().trim()));
        alerts.lead("CONSULTATION", "New consultation request", "Consultation from "+r.email().trim().toLowerCase(Locale.ROOT));
        return saved;
    }
    /** The API is private behind the trusted Nginx proxy; do not trust client-supplied forwarding headers. */
    private String clientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    @GetMapping("/admin")
    public List<ConsultationRequest> adminList(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="50") int size, Authentication auth){
        if(auth==null || auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"))) throw new org.springframework.security.access.AccessDeniedException("Admin access required");
        return repo.findAll(org.springframework.data.domain.PageRequest.of(Math.max(0,page),Math.min(Math.max(1,size),100),org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,"createdAt"))).getContent();
    }
    @PutMapping("/admin/{id}/status")
    public ConsultationRequest adminStatus(@PathVariable Long id,@RequestBody StatusBody body,Authentication auth){
        if(auth==null || auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"))) throw new org.springframework.security.access.AccessDeniedException("Admin access required");
        String status=body.status()==null?"NEW":body.status().trim().toUpperCase(Locale.ROOT);
        if(!Set.of("NEW","CONTACTED","IN_PROGRESS","COMPLETED","CLOSED").contains(status)) throw new IllegalArgumentException("Invalid consultation status");
        var item=repo.findById(id).orElseThrow(() -> new NoSuchElementException("Consultation request not found"));
        if (!allowedTransition(item.getStatus(), status)) throw new IllegalArgumentException("Invalid consultation state transition");
        item.status(status); return repo.save(item);
    }
    private static boolean allowedTransition(String from, String to) {
        if (from.equals(to)) return true;
        return switch (from) {
            case "NEW" -> Set.of("CONTACTED","CLOSED").contains(to);
            case "CONTACTED" -> Set.of("IN_PROGRESS","CLOSED").contains(to);
            case "IN_PROGRESS" -> Set.of("COMPLETED","CLOSED").contains(to);
            case "COMPLETED" -> Set.of("CLOSED").contains(to);
            case "CLOSED" -> false;
            default -> false;
        };
    }
    public record StatusBody(@NotBlank String status){}
}

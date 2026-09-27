package com.wolfe.api;

import com.wolfe.security.RateLimitService;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<Map<String, String>> notFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "NOT_FOUND", "message", ex.getMessage()));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", "VALIDATION_ERROR", "message", ex.getMessage()));
    }
    @ExceptionHandler(RateLimitService.RateLimitExceededException.class)
    ResponseEntity<Map<String, String>> rateLimited() {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
        .header(HttpHeaders.RETRY_AFTER, "900")
        .body(Map.of("error", "RATE_LIMITED", "message", "Too many authentication attempts. Try again later."));
    }
}

package com.wolfe.api;

import com.wolfe.security.RateLimitService;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.dao.DataIntegrityViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);
    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<Map<String, String>> notFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "NOT_FOUND", "message", "Resource not found"));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", "VALIDATION_ERROR", "message", safeClientMessage(ex.getMessage())));
    }
    private static String safeClientMessage(String message) {
        if (message == null || message.isBlank()) return "Invalid request";
        // Only expose a small allow-list of intentionally user-facing validation text.
        // Internal identifiers (numeric, UUID, WLF-* order IDs, SKUs, file paths, etc.)
        // must never be reflected through exception messages.
        String m = message.trim();
        String lower = m.toLowerCase(java.util.Locale.ROOT);
        if (lower.contains("sql") || lower.contains("jdbc") || lower.contains("constraint") ||
                lower.contains("exception") || lower.contains("stack") || lower.contains("path") ||
                lower.contains("token") || lower.matches(".*\\b(id|order|customer|product|retailer|item|job|sku)\\b.*")) {
            return "Invalid request";
        }
        return switch (m) {
            case "quantity must be between 1 and 100 per order line",
                 "quantity must be between 1 and 100",
                 "order must contain at least one item",
                 "complete customer and delivery details are required",
                 "Idempotency-Key is required",
                 "password must be at most 72 UTF-8 bytes" -> m;
            default -> "Invalid request";
        };
    }
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<Map<String, String>> denied() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "FORBIDDEN", "message", "Access denied"));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> invalidRequest() {
        return ResponseEntity.badRequest()
                .body(Map.of("error", "VALIDATION_ERROR", "message", "Request validation failed"));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String, String>> integrityConflict() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "CONFLICT", "message", "The requested resource conflicts with an existing record; please retry."));
    }

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<Map<String, String>> conflict(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "CONFLICT", "message", safeClientMessage(ex.getMessage())));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Map<String, String>> malformedJson() {
        return ResponseEntity.badRequest().body(Map.of("error", "VALIDATION_ERROR", "message", "Malformed request body"));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<Map<String, String>> missingParameter(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", "VALIDATION_ERROR", "message", "Missing request parameter: " + ex.getParameterName()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<Map<String, String>> methodNotAllowed() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(Map.of("error", "METHOD_NOT_ALLOWED", "message", "HTTP method is not supported for this endpoint"));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<Map<String, String>> resourceNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "NOT_FOUND", "message", "Endpoint not found"));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<Map<String, String>> uploadTooLarge() {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(Map.of("error", "PAYLOAD_TOO_LARGE", "message", "Uploaded file exceeds the allowed size"));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, String>> unexpected(Exception ex) {
        log.error("Unhandled API exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "INTERNAL_SERVER_ERROR", "message", "An unexpected server error occurred"));
    }

    @ExceptionHandler(RateLimitService.RateLimitUnavailableException.class)
    ResponseEntity<Map<String, String>> rateLimitUnavailable() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .header(HttpHeaders.RETRY_AFTER, "60")
                .body(Map.of("error", "RATE_LIMIT_UNAVAILABLE", "message", "Security rate limiting is temporarily unavailable; please retry shortly."));
    }
    @ExceptionHandler(RateLimitService.RateLimitExceededException.class)
    ResponseEntity<Map<String, String>> rateLimited() {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
        .header(HttpHeaders.RETRY_AFTER, "900")
        .body(Map.of("error", "RATE_LIMITED", "message", "Too many requests. Try again later."));
    }
}

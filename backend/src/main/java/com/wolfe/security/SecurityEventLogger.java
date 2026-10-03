package com.wolfe.security;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SecurityEventLogger {
    private static final Logger log = LoggerFactory.getLogger("WOLFE_SECURITY_EVENT");
    public void denied(HttpServletRequest req, int status, String reason) {
        log.warn("event=security_denied status={} method={} path={} remote={} reason={}", status, req.getMethod(), req.getRequestURI(), req.getRemoteAddr(), reason);
    }
    public void loginSuccess(Long customerId, HttpServletRequest req) {
        log.info("event=login_success customerId={} method={} path={} remote={}", customerId, req.getMethod(), req.getRequestURI(), req.getRemoteAddr());
    }
    public void refreshSuccess(HttpServletRequest req) {
        log.info("event=refresh_success method={} path={} remote={}", req.getMethod(), req.getRequestURI(), req.getRemoteAddr());
    }
    public void logout(Long customerId, HttpServletRequest req) {
        log.info("event=logout customerId={} method={} path={} remote={}", customerId, req.getMethod(), req.getRequestURI(), req.getRemoteAddr());
    }
    public void refreshReuse(Long customerId, HttpServletRequest req) {
        log.error("event=refresh_token_reuse customerId={} method={} path={} remote={}", customerId, req.getMethod(), req.getRequestURI(), req.getRemoteAddr());
    }
}

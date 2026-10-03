package com.wolfe.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuditInterceptor implements HandlerInterceptor {
    private final AdminAuditLogRepository repo;
    public AdminAuditInterceptor(AdminAuditLogRepository repo){this.repo=repo;}
    @Override public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String path=request.getRequestURI();
        if (!path.startsWith("/api/v1/admin/") || "GET".equalsIgnoreCase(request.getMethod())) return;
        Authentication auth=org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth==null || !auth.isAuthenticated()) return;
        Long actorId=null;
        Object details=auth.getDetails();
        if (details instanceof Long id) actorId=id;
        String action=request.getMethod()+" "+path;
        repo.save(new AdminAuditLog(actorId, auth.getName(), request.getMethod(), path, response.getStatus(), action));
    }
}

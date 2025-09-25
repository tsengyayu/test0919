package com.example.test0919.aop;

import com.example.test0919.dto.AuditContextHolder;
import com.example.test0919.dto.AuditLogger;
import com.example.test0919.dto.PendingAudit;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.LOWEST_PRECEDENCE) // 越晚越能拿到最終 status
public class ApiAuditWriteFilter extends OncePerRequestFilter {

    private final AuditLogger auditLogger;

    public ApiAuditWriteFilter(AuditLogger auditLogger) {
        this.auditLogger = auditLogger;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            try {
                if (!AuditContextHolder.isEmpty()) {
                    int status = response.getStatus(); // 最終 HTTP 狀態碼
                    for (PendingAudit p : AuditContextHolder.getAll()) {
                        auditLogger.writeNow(p, status);
                    }
                }
            } finally {
                AuditContextHolder.clear(); // 一定要清掉，避免 ThreadLocal 泄漏
            }
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        // 只記錄你的業務 API（必要時調整）
        return !uri.startsWith("/api/");
    }
}

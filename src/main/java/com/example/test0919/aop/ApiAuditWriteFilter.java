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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

        // 事先抓住使用者（此時 SecurityContext 還在）
        final String actorAtEntry = currentUser();

        try {
            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            // ⬅️ 關鍵：任何在進入 Service 前/中發生的錯，都補一筆 PendingAudit
            String err = safeMessage(ex);
            // 你也可以嘗試從 path 拿個 id；這裡先寫 N/A
            AuditContextHolder.add(new PendingAudit(
                    "ERROR", "N/A", null, null, err, actorAtEntry
            ));
            // 繼續丟出去，讓 @ControllerAdvice 決定狀態碼與回應
            throw ex;

        } finally {
            try {
                int status = response.getStatus(); // 最終 HTTP 狀態碼（成功或失敗）

                // 有 AOP 累積到的就逐筆寫
                if (!AuditContextHolder.isEmpty()) {
                    for (PendingAudit p : AuditContextHolder.getAll()) {
                        auditLogger.writeNow(p, status);
                    }
                } else {
                    // 少數情況（例如某些框架層直接短路），保底也可記一筆「純狀態」稽核
                    // （若不想重覆，可保留空集合就不寫）
                    // auditLogger.writeNow(new PendingAudit("REQUEST", "N/A", null, null, null, actorAtEntry), status);
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

    private String currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null && !auth.getName().isBlank())
                ? auth.getName() : "anonymous";
    }

    private String safeMessage(Throwable ex) {
        // 收斂一下訊息，避免過長
        String msg = ex.getMessage();
        if (msg == null || msg.isBlank()) msg = ex.getClass().getSimpleName();
        return msg.length() <= 4000 ? msg : msg.substring(0, 4000);
    }
}
package com.example.test0919.aop;

import com.example.test0919.dto.AuditContextHolder;
import com.example.test0919.dto.AuditLogger;
import com.example.test0919.dto.PendingAudit;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApiAuditWriteFilterTest {

    AuditLogger auditLogger;
    ApiAuditWriteFilter filter;
    HttpServletRequest request;
    HttpServletResponse response;
    FilterChain chain;

    @BeforeEach
    void setUp() {
        auditLogger = mock(AuditLogger.class);
        filter = new ApiAuditWriteFilter(auditLogger);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);

        // 預設走 /api 路徑、200 狀態
        when(request.getRequestURI()).thenReturn("/api/anything");
        when(response.getStatus()).thenReturn(200);

        // 清理 ThreadLocal，避免不同測試互相污染
        AuditContextHolder.clear();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        AuditContextHolder.clear();
        SecurityContextHolder.clearContext();
    }

    /* ---------- 小工具：設定當前使用者 ---------- */
    private void setAuthUser(String username) {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(username);
        SecurityContext sc = mock(SecurityContext.class);
        when(sc.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(sc);
    }

    /* ========== 測試 1：成功請求 + 有 PendingAudit → 會寫庫 ========== */
    @Test
    void success_withPendingAudit_writesOnceAndClearsContext() throws ServletException, IOException {
        setAuthUser("alice");

        // 預先放一筆 PendingAudit（模擬 AOP 已經塞好）
        PendingAudit pa = new PendingAudit("UPDATE", 123, "before", "after", null, "alice");
        AuditContextHolder.add(pa);

        filter.doFilter(request, response, chain);

        // 驗證 auditLogger 被呼叫一次，status=200
        verify(auditLogger, times(1)).writeNow(pa, 200);

        // 驗證 Pending 清乾淨
        assertTrue(AuditContextHolder.isEmpty());
    }

    /* ========== 測試 2：成功請求 + 無 PendingAudit → 不寫庫但要清乾淨 ========== */
    @Test
    void success_withoutPendingAudit_writesNothing_andClearsContext() throws ServletException, IOException {
        setAuthUser("bob");

        filter.doFilter(request, response, chain);

        verify(auditLogger, never()).writeNow(any(), anyInt());
        assertTrue(AuditContextHolder.isEmpty());
    }

    /* ========== 測試 3：鏈上丟例外（5xx）→ 補一筆 ERROR 並寫庫 ========== */
    @Test
    void exception_path_addsErrorPending_andWritesWithFinalStatus() throws ServletException, IOException {
        setAuthUser("charlie");

        // 模擬鏈上丟例外（例如參數綁定錯），response 最終狀態假設由上層轉成 500
        RuntimeException boom = new RuntimeException("boom!");
        doThrow(boom).when(chain).doFilter(request, response);
        when(response.getStatus()).thenReturn(500);

        // 驗證會把例外再拋出（符合 filter 設計）
        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> filter.doFilter(request, response, chain));
        assertEquals("boom!", thrown.getMessage());

        // 捕捉寫庫時的 PendingAudit，確認是 ERROR & 帶錯誤訊息
        ArgumentCaptor<PendingAudit> pac = ArgumentCaptor.forClass(PendingAudit.class);
        verify(auditLogger, times(1)).writeNow(pac.capture(), eq(500));

        PendingAudit captured = pac.getValue();
        assertEquals("ERROR", captured.action);
        assertEquals("N/A", captured.entityId);
        assertEquals("boom!", captured.errorMessage);
        assertEquals("charlie", captured.actorName);

        // ThreadLocal 清掉
        assertTrue(AuditContextHolder.isEmpty());
    }

    /* ========== 測試 4：例外訊息為 null → safeMessage 會回類名 ========== */
    @Test
    void exception_withNullMessage_usesClassSimpleNameAsErrorMessage() throws ServletException, IOException {
        setAuthUser("dora");

        // 模擬訊息為 null 的例外
        RuntimeException boom = new RuntimeException((String) null);
        doThrow(boom).when(chain).doFilter(request, response);
        when(response.getStatus()).thenReturn(500);

        assertThrows(RuntimeException.class, () -> filter.doFilter(request, response, chain));

        ArgumentCaptor<PendingAudit> pac = ArgumentCaptor.forClass(PendingAudit.class);
        verify(auditLogger).writeNow(pac.capture(), eq(500));
        assertEquals("RuntimeException", pac.getValue().errorMessage); // safeMessage 生效
        assertEquals("dora", pac.getValue().actorName);
    }

    /* ========== 測試 5：非 /api 路徑 → 不進過濾（shouldNotFilter=true） ========== */
    @Test
    void nonApiPath_isSkippedByFilter() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/public/health");

        // 這邊用 doFilter（父類別會先調 shouldNotFilter）
        filter.doFilter(request, response, chain);

        // 不會寫庫
        verify(auditLogger, never()).writeNow(any(), anyInt());
        // 不會往 AuditContextHolder 塞資料（我們也沒放）
        assertTrue(AuditContextHolder.isEmpty());

        // 應該仍會呼叫下一個 filter/chain
        verify(chain, times(1)).doFilter(request, response);
    }

    /* ========== 測試 6：匿名使用者 → actor 會是 anonymous ========== */
    @Test
    void anonymousUser_isRecordedAsAnonymousInErrorPath() throws ServletException, IOException {
        // 不設 SecurityContext（匿名）
        doThrow(new IllegalStateException("no auth")).when(chain).doFilter(request, response);
        when(response.getStatus()).thenReturn(500);

        assertThrows(IllegalStateException.class, () -> filter.doFilter(request, response, chain));

        ArgumentCaptor<PendingAudit> pac = ArgumentCaptor.forClass(PendingAudit.class);
        verify(auditLogger).writeNow(pac.capture(), eq(500));
        assertEquals("anonymous", pac.getValue().actorName);
        assertEquals("no auth", pac.getValue().errorMessage);
    }
}
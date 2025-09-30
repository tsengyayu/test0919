package com.example.test0919.aop;

import com.example.test0919.dto.AuditContextHolder;
import com.example.test0919.dto.PendingAudit;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentMatchers;
import org.mockito.stubbing.Answer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.CodeSignature;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 單元測試重點：
 * 1) 檢查 AuditAspect 對各 action 的 before/after/actor/errorMessage 行為
 * 2) 驗證 idArg、idFromReturn、fixedEntityId、loader 解析是否正確
 * 3) 驗證例外路徑（proceed() 丟例外）會把 errorMessage 寫入 PendingAudit
 * 4) 驗證 idArg 找不到時丟 IllegalStateException、不寫入
 * 5) 驗證 loader 在取 before 就失敗時（目前切面 before 在 try 外）不會寫入（直接拋出）
 */
class AuditAspectTest {

    private AuditAspect aspect;

    // 測試用目標物件（提供 loader 方法給切面用）
    static class DummyTarget {
        public Product getById(Integer id) {         // ← 改成 public
            return new Product(id, "P" + id);
        }
        public Product getByIdPrimitive(int id) {    // ← 改成 public（如果要測 primitive）
            return new Product(id, "Pi" + id);
        }
        public Product getByName(String name) {      // ← 改成 public
            return new Product(999, name);
        }
        public Product getNullById(Integer id) {     // ← 改成 public
            return null;
        }
        public Product throwInLoader(Integer id) {   // ← 改成 public
            throw new RuntimeException("loader boom");
        }
    }

    // 簡單的 POJO，方便看 before/after
    static class Product {
        final Integer id;
        final String name;
        Product(Integer id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return "Product(" + id + "," + name + ")"; }
    }

    @BeforeEach
    void setUp() {
        aspect = new AuditAspect();
        AuditContextHolder.clear();
        mockUser("tester");
    }

    @AfterEach
    void tearDown() {
        AuditContextHolder.clear();
        SecurityContextHolder.clearContext();
    }

    private void mockUser(String username) {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(username);
        SecurityContext sc = mock(SecurityContext.class);
        when(sc.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(sc);
    }

    private ProceedingJoinPoint mockPjp(Object target, String[] paramNames, Object[] args, Object proceedReturn) throws Throwable {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        CodeSignature sig = mock(CodeSignature.class);
        when(sig.getParameterNames()).thenReturn(paramNames);
        when(pjp.getSignature()).thenReturn(sig);
        when(pjp.getArgs()).thenReturn(args);
        when(pjp.getTarget()).thenReturn(target);
        if (proceedReturn instanceof Throwable t) {
            when(pjp.proceed()).thenThrow(t);
        } else {
            when(pjp.proceed()).thenReturn(proceedReturn);
        }
        return pjp;
    }

    private Auditable mockAnn(String action, String idArg, boolean idFromReturn, String loader, String fixedEntityId) {
        Auditable a = mock(Auditable.class);
        when(a.action()).thenReturn(action);
        when(a.idArg()).thenReturn(idArg);
        when(a.idFromReturn()).thenReturn(idFromReturn);
        when(a.loader()).thenReturn(loader);
        when(a.fixedEntityId()).thenReturn(fixedEntityId);
        return a;
    }

    /* ========== READ：單筆 ==========
       idArg = productId，after = ret，entityId = productId
     */
    @Test
    void read_single_usesReturnAsAfter_andIdFromArg() throws Throwable {
        DummyTarget target = new DummyTarget();
        int productId = 7;
        Product ret = new Product(productId, "read7");

        ProceedingJoinPoint pjp = mockPjp(target,
                new String[]{"productId"}, new Object[]{productId}, ret);
        Auditable ann = mockAnn("READ", "productId", false, "getById", "");

        Object out = aspect.around(pjp, ann);
        assertSame(ret, out);

        List<PendingAudit> list = AuditContextHolder.getAll();
        assertEquals(1, list.size());
        PendingAudit pa = list.get(0);
        assertEquals("READ", pa.action);
        assertEquals(String.valueOf(productId), String.valueOf(pa.entityId));
        assertNull(pa.beforeObj);
        assertSame(ret, pa.afterObj);
        assertNull(pa.errorMessage);
        assertEquals("tester", pa.actorName);
    }

    /* ========== READ：列表（無 idArg，使用 fixedEntityId） ==========
       entityId = fixedEntityId, after = ret
     */
    @Test
    void read_list_usesFixedEntityId() throws Throwable {
        DummyTarget target = new DummyTarget();
        List<Product> ret = List.of(new Product(1, "a"), new Product(2, "b"));

        ProceedingJoinPoint pjp = mockPjp(target, new String[]{}, new Object[]{}, ret);
        Auditable ann = mockAnn("READ", "", false, "getById", "ALL_PRODUCTS");

        Object out = aspect.around(pjp, ann);
        assertSame(ret, out);

        PendingAudit pa = AuditContextHolder.getAll().get(0);
        assertEquals("READ", pa.action);
        assertEquals("ALL_PRODUCTS", pa.entityId);
        assertNull(pa.beforeObj);
        assertSame(ret, pa.afterObj);
        assertNull(pa.errorMessage);
    }

    /* ========== CREATE：idFromReturn=true，loader 取 after ==========
       ret=新 id；after = loader(id)
     */
    @Test
    void create_idFromReturn_loaderGetsAfter() throws Throwable {
        DummyTarget target = new DummyTarget();
        Integer newId = 10;

        ProceedingJoinPoint pjp = mockPjp(target,
                new String[]{}, new Object[]{}, newId);
        Auditable ann = mockAnn("CREATE", "", true, "getById", "");

        Object out = aspect.around(pjp, ann);
        assertEquals(newId, out);

        PendingAudit pa = AuditContextHolder.getAll().get(0);
        assertEquals("CREATE", pa.action);
        assertEquals(String.valueOf(newId), String.valueOf(pa.entityId));
        assertNull(pa.beforeObj);
        assertTrue(pa.afterObj instanceof Product);
        assertNull(pa.errorMessage);
    }

    /* ========== UPDATE：正常路徑 ==========
       before = loader(id)（proceed 前），after = loader(id)（proceed 後）
     */
    @Test
    void update_success_hasBeforeAndAfter() throws Throwable {
        DummyTarget target = spy(new DummyTarget());

        int id = 5;
        // proceed() 不回任何東西（void 方法），模擬回 null
        ProceedingJoinPoint pjp = mockPjp(target,
                new String[]{"productId"}, new Object[]{id}, null);
        Auditable ann = mockAnn("UPDATE", "productId", false, "getById", "");

        Object out = aspect.around(pjp, ann);
        assertNull(out);

        PendingAudit pa = AuditContextHolder.getAll().get(0);
        assertEquals("UPDATE", pa.action);
        assertEquals(String.valueOf(id), String.valueOf(pa.entityId));
        assertTrue(pa.beforeObj instanceof Product);
        assertTrue(pa.afterObj instanceof Product);
        assertNull(pa.errorMessage);
    }

    /* ========== DELETE：before 有值，after = null ==========
     */
    @Test
    void delete_success_hasBefore_afterIsNull() throws Throwable {
        DummyTarget target = new DummyTarget();

        String name = "toDel";
        ProceedingJoinPoint pjp = mockPjp(target,
                new String[]{"productName"}, new Object[]{name}, null);
        Auditable ann = mockAnn("DELETE", "productName", false, "getByName", "");

        Object out = aspect.around(pjp, ann);
        assertNull(out);

        PendingAudit pa = AuditContextHolder.getAll().get(0);
        assertEquals("DELETE", pa.action);
        assertEquals(name, pa.entityId);
        assertTrue(pa.beforeObj instanceof Product);
        assertNull(pa.afterObj);
        assertNull(pa.errorMessage);
    }

    /* ========== proceed() 丟例外：會寫 errorMessage；after=null ==========
     */
    @Test
    void proceedThrows_errorIsCaptured() throws Throwable {
        DummyTarget target = new DummyTarget();
        int id = 88;

        ProceedingJoinPoint pjp = mockPjp(target,
                new String[]{"productId"}, new Object[]{id}, new RuntimeException("boom"));
        Auditable ann = mockAnn("UPDATE", "productId", false, "getById", "");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> aspect.around(pjp, ann));
        assertEquals("boom", ex.getMessage());

        PendingAudit pa = AuditContextHolder.getAll().get(0);
        assertEquals("UPDATE", pa.action);
        assertEquals(String.valueOf(id), String.valueOf(pa.entityId));
        assertTrue(pa.beforeObj instanceof Product); // before 仍在 try 外，已取到
        assertNull(pa.afterObj);
        assertEquals("boom", pa.errorMessage);
        assertEquals("tester", pa.actorName);
    }

    /* ========== idArg 指定了，但參數名找不到：丟 IllegalStateException，不應寫入 ==========
     */
    @Test
    void idArgNotFound_throwsIllegalState_andNoAudit() throws Throwable {
        DummyTarget target = new DummyTarget();

        ProceedingJoinPoint pjp = mockPjp(target,
                new String[]{"x", "y"}, new Object[]{1, 2}, null);
        Auditable ann = mockAnn("UPDATE", "productId", false, "getById", "");

        assertThrows(IllegalStateException.class, () -> aspect.around(pjp, ann));
        assertTrue(AuditContextHolder.isEmpty());
    }

    /* ========== loader 失敗在「取 before」階段：切面 before 在 try 外 → 直接丟錯 & 不寫入 ==========
     */
    @Test
    void loaderThrowsBefore_tryOutside_behaviorIsThrow_andNoAudit() throws Throwable {
        DummyTarget target = new DummyTarget();

        ProceedingJoinPoint pjp = mockPjp(target,
                new String[]{"productId"}, new Object[]{1}, null);
        // 指向會丟例外的 loader
        Auditable ann = mockAnn("UPDATE", "productId", false, "throwInLoader", "");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> aspect.around(pjp, ann));
        assertTrue(ex.getMessage().contains("呼叫 loader 失敗"));
        assertTrue(AuditContextHolder.isEmpty());
    }
}
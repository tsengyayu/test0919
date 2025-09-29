//package com.example.test0919.aop;
//
//import com.example.test0919.dto.AuditContextHolder;
//import com.example.test0919.dto.PendingAudit;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.reflect.CodeSignature;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//
//import java.lang.reflect.Method;
//import java.util.Locale;
//import java.util.Objects;
//
//@Aspect
//@Component
//public class AuditAspect {
//
//    @Around("@annotation(auditable)")
//    public Object around(ProceedingJoinPoint pjp, Auditable auditable) throws Throwable {
//        String action = auditable.action().toUpperCase(Locale.ROOT);
//        boolean isRead   = "READ".equals(action);
//        boolean isCreate = "CREATE".equals(action);
//        boolean isDelete = "DELETE".equals(action);
//
//        Object target = pjp.getTarget();
//
//        // 取得方法參數中的 entityId（若需要）
//        Object id = null;
//        if (!auditable.idFromReturn() && !auditable.idArg().isBlank()) {
//            id = getArgByName(pjp, auditable.idArg());
//            if (id == null) {
//                throw new IllegalStateException("找不到參數 '" + auditable.idArg() + "' 當作 entityId");
//            }
//        }
//
//        // READ 不需要 loader；其他動作需要用 loader 取 before/after
//        Method loader = null;
//        if (!isRead) {
//            loader = resolveLoader(target, auditable.loader());
//        }
//
//        // 取 before（UPDATE/DELETE 需要）
//        Object beforeObj = null;
//        if (!isCreate && !isRead) {
//            beforeObj = safeInvokeLoader(loader, target, id);
//        }
//
//        // 這個時間點 SecurityContext 還在，先抓好使用者
//        String actorName = currentUser();
//
//        try {
//            // 執行原方法
//            Object ret = pjp.proceed();
//
//            // CREATE：回傳值就是新 id
//            if (auditable.idFromReturn()) {
//                id = ret;
//            }
//
//            // after：READ 直接用回傳資料；DELETE 沒有；其餘用 loader 再取一次
//            Object afterObj;
//            if (isRead) {
//                afterObj = ret;
//            } else if (isDelete) {
//                afterObj = null;
//            } else {
//                afterObj = safeInvokeLoader(loader, target, id);
//            }
//
//            // READ 的 entityId：若沒指定 idArg，給固定或 N/A
//            if (isRead && (id == null || String.valueOf(id).isBlank())) {
//                String fixed = getFixedEntityId(auditable);
//                id = (fixed != null && !fixed.isBlank()) ? fixed : "N/A";
//            }
//
//            // 暫存一筆待寫入（由 Filter 依最終 httpStatus 落庫）
//            AuditContextHolder.add(new PendingAudit(action, id, beforeObj, afterObj, null, actorName));
//            return ret;
//
//        } catch (Exception ex) {
//            // 失敗也記（errorMessage 帶進去；after=null）
//            AuditContextHolder.add(new PendingAudit(action, id, beforeObj, null, ex.getMessage(), actorName));
//            throw ex;
//        }
//    }
//
//    /* ---------- Helpers ---------- */
//
//    private Object getArgByName(ProceedingJoinPoint pjp, String name) {
//        CodeSignature sig = (CodeSignature) pjp.getSignature();
//        String[] names = sig.getParameterNames();
//        Object[] args  = pjp.getArgs();
//        for (int i = 0; i < names.length; i++) {
//            if (Objects.equals(names[i], name)) return args[i];
//        }
//        return null;
//    }
//
//    private Method resolveLoader(Object target, String loaderName) {
//        Class<?> clazz = target.getClass();
//        // 嘗試常見參數型別：Integer/Long/String
//        try { return clazz.getMethod(loaderName, Integer.class); } catch (NoSuchMethodException ignored) {}
//        try { return clazz.getMethod(loaderName, int.class); }     catch (NoSuchMethodException ignored) {}
//        try { return clazz.getMethod(loaderName, Long.class); }    catch (NoSuchMethodException ignored) {}
//        try { return clazz.getMethod(loaderName, long.class); }    catch (NoSuchMethodException ignored) {}
//        try { return clazz.getMethod(loaderName, String.class); }  catch (NoSuchMethodException ignored) {}
//        throw new IllegalStateException("在 " + clazz.getSimpleName() + " 找不到 loader: " + loaderName + "(id/name)");
//    }
//
//    private Object safeInvokeLoader(Method loader, Object target, Object id) {
//        try {
//            Class<?> t = loader.getParameterTypes()[0];
//            Object coerced = coerce(id, t);
//            return loader.invoke(target, coerced);
//        } catch (Exception e) {
//            throw new RuntimeException("呼叫 loader 失敗: " + loader.getName() + " id=" + id, e);
//        }
//    }
//
//    private Object coerce(Object id, Class<?> t) {
//        if (id == null) return null;
//        if (t.isInstance(id)) return id;
//        String s = String.valueOf(id);
//        if (t == Integer.class || t == int.class) return Integer.parseInt(s);
//        if (t == Long.class    || t == long.class) return Long.parseLong(s);
//        if (t == String.class) return s;
//        return id;
//    }
//
//    private String currentUser() {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        return (auth != null && auth.getName() != null && !auth.getName().isBlank())
//                ? auth.getName() : "anonymous";
//    }
//
//    // 為了相容你的 @Auditable 若有 fixedEntityId 屬性（列表 READ 可標記 e.g. "ALL_PRODUCTS"）
//    private String getFixedEntityId(Auditable auditable) {
//        try {
//            return auditable.fixedEntityId(); // 如果你的註解沒有這個屬性，這裡會進 catch，回傳 null
//        } catch (Throwable ignore) {
//            return null;
//        }
//    }
//    }
//

//package com.example.test0919.dto;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//public class AuditContextHolder {
//    private static final ThreadLocal<List<PendingAudit>> CTX = ThreadLocal.withInitial(ArrayList::new);
//
//    public static void add(PendingAudit p) { CTX.get().add(p); }
//
//    public static List<PendingAudit> getAll() { return Collections.unmodifiableList(CTX.get()); }
//
//    public static boolean isEmpty() { return CTX.get().isEmpty(); }
//
//    public static void clear() { CTX.remove(); }
//}

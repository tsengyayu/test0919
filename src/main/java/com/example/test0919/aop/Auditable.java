//package com.example.test0919.aop;
//
//import java.lang.annotation.*;
//
//@Target(ElementType.METHOD)
//@Retention(RetentionPolicy.RUNTIME)
//public @interface Auditable {
//    String action();              // CREATE / UPDATE / DELETE
//    String idArg() default "";    // id 參數名稱（UPDATE/DELETE 用）
//    boolean idFromReturn() default false; // CREATE：回傳值就是新 id
//    String loader() default "findById";   // 載入單筆的方法名
//    String fixedEntityId() default "";
//}

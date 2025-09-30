package com.example.test0919.logging;

import java.util.Map;
import java.util.regex.Pattern;

public class SensitiveDataMasker {
    private static final String MASK = "****";

    private static final Map<String, Pattern> patterns = Map.of(
            "email", Pattern.compile("([a-zA-Z0-9_.+-]+)@([a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+)"),
            "phone", Pattern.compile("\\b\\+?\\d{8,15}\\b"),
            "bearer", Pattern.compile("(?i)bearer\\s+[a-z0-9\\-_.=:+/]{10,}"),
            "id", Pattern.compile("\\b\\d{10,}\\b")
    );

    public static String mask(String s){
        if (s == null || s.isBlank()) return s;
        String r = s;
        for (Pattern p : patterns.values()) {
            r = p.matcher(r).replaceAll(MASK);
        }
        return r;
    }
}

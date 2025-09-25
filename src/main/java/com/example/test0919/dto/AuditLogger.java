package com.example.test0919.dto;

import com.example.test0919.dao.AuditLogDao;
import com.example.test0919.model.AuditLog;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuditLogger {
    private final AuditLogDao auditLogDao;
    private final ObjectMapper om;

    public AuditLogger(AuditLogDao auditLogDao, ObjectMapper objectMapper) {
        this.auditLogDao = auditLogDao;
        this.om = objectMapper.copy().setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public void writeNow(PendingAudit p, int httpStatus) {
        String actor = (p.actorName != null && !p.actorName.isBlank())
                ? p.actorName : currentUser();

        String entityId  = (p.entityId == null || String.valueOf(p.entityId).isBlank())
                ? "N/A" : String.valueOf(p.entityId);
        String beforeJson = toJson(p.beforeObj);
        String afterJson  = toJson(p.afterObj);
        String statusStr  = String.valueOf(httpStatus);
        String err        = truncate(p.errorMessage, 4000);
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(actor);
        auditLog.setEntityId(entityId);
        auditLog.setBeforeData(beforeJson);
        auditLog.setAfterData(afterJson);
        auditLog.setHttpStatus(statusStr);
        auditLog.setErrorMessage(err);

        auditLogDao.insertAuditLog(auditLog);
    }

    private String currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getName() != null) ? auth.getName() : "anonymous";
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try { return om.writeValueAsString(obj); } catch (Exception e) { return "{\"_ser\":\"fail\"}"; }
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}

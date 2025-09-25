package com.example.test0919.service.impl;

import com.example.test0919.dao.AuditLogDao;
import com.example.test0919.model.AuditLog;
import com.example.test0919.service.AuditLogService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuditLogServiceImpl implements AuditLogService {

    private AuditLogDao auditLogDao;

    @Override
    public void saveAuditLog(AuditLog auditLog) {
        auditLogDao.insertAuditLog(auditLog);





    }
}

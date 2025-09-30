package com.example.test0919.dao.Impl;

import com.example.test0919.dao.AuditLogDao;
import com.example.test0919.model.AuditLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AuditLogDaoImpl implements AuditLogDao {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public void insertAuditLog(AuditLog auditLog) {
        String sql = "INSERT INTO audit_log(actor_name, action, entity_id, before_data, " +
                "after_data, http_status, error_message) VALUES(:actorName, :action, :entityId, :beforeData," +
                ":afterData, :httpStatus, :errorMessage)";

        Map<String, Object> map = new HashMap<>();
        map.put("actorName", auditLog.getActorName());
        map.put("action", auditLog.getAction());
        map.put("entityId", auditLog.getEntityId());
        map.put("beforeData", auditLog.getBeforeData());
        map.put("afterData", auditLog.getAfterData());
        map.put("httpStatus", auditLog.getHttpStatus());
        map.put("errorMessage", auditLog.getErrorMessage());

        namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(map));


    }
}

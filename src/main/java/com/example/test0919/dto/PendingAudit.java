package com.example.test0919.dto;

public class PendingAudit {

    public String action;
    public Object entityId;
    public Object beforeObj;
    public Object afterObj;
    public String errorMessage; // 有錯就填，沒錯就 null
    public String actorName;

    public PendingAudit(String action, Object entityId, Object beforeObj, Object afterObj, String errorMessage, String actorName) {
        this.action = action;
        this.entityId = entityId;
        this.beforeObj = beforeObj;
        this.afterObj = afterObj;
        this.errorMessage = errorMessage;
        this.actorName = actorName;
    }
}

package com.example.test0919.dto;

import java.util.List;

public class EntitlementDto {
    public Long id;
    public String tittName;
    private Long functionGroupId;// 依你原鍵名
    public java.util.List<String> de;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFunctionGroupId() {
        return functionGroupId;
    }

    public void setFunctionGroupId(Long functionGroupId) {
        this.functionGroupId = functionGroupId;
    }

    public String getTittName() {
        return tittName;
    }

    public void setTittName(String tittName) {
        this.tittName = tittName;
    }

    public List<String> getDe() {
        return de;
    }

    public void setDe(List<String> de) {
        this.de = de;
    }
}

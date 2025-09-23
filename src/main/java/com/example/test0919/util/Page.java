package com.example.test0919.util;

import java.util.List;

public class Page<T> {
    private Integer linmit;
    private Integer offset;
    private  Integer total;
    private List<T> result;

    public Integer getLinmit() {
        return linmit;
    }

    public void setLinmit(Integer linmit) {
        this.linmit = linmit;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<T> getResult() {
        return result;
    }

    public void setResult(List<T> result) {
        this.result = result;
    }
}

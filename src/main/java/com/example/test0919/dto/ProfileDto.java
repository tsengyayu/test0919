package com.example.test0919.dto;

import java.util.List;

public class ProfileDto {
    public Long id;
    public String name;
    public int age;
    public boolean isContain;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public boolean isContain() {
        return isContain;
    }

    public void setContain(boolean contain) {
        isContain = contain;
    }
}

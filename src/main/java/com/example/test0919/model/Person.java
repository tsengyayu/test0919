package com.example.test0919.model;

public class Person {
    private String id;
    private String name;
    private Integer age;
    private boolean isMarried;
    private String friedns;
    private String family;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public boolean isMarried() {
        return isMarried;
    }

    public void setMarried(boolean married) {
        isMarried = married;
    }

    public String getFriedns() {
        return friedns;
    }

    public void setFriedns(String friedns) {
        this.friedns = friedns;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }
}

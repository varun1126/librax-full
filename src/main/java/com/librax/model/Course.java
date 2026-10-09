package com.librax.model;

public class Course {
    private Integer id;
    private Integer departmentId;
    private String name;
    private String code;

    public Course() {}

    public Course(Integer id, Integer departmentId, String name, String code) {
        this.id = id;
        this.departmentId = departmentId;
        this.name = name;
        this.code = code;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}

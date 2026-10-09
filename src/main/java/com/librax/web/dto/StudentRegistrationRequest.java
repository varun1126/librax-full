package com.librax.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class StudentRegistrationRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email address is required")
    @Email(message = "Valid email address is required")
    private String email;

    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String studentId;
    private String membershipNumber;
    private String phone;
    private String department;
    private String departmentName;
    private String courseName;

    public StudentRegistrationRequest() {}

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getStudentId() {
        return studentId != null && !studentId.isBlank() ? studentId : membershipNumber;
    }
    public void setStudentId(String studentId) {
        this.studentId = studentId;
        if (this.membershipNumber == null || this.membershipNumber.isBlank()) {
            this.membershipNumber = studentId;
        }
    }

    public String getMembershipNumber() {
        return membershipNumber != null && !membershipNumber.isBlank() ? membershipNumber : studentId;
    }
    public void setMembershipNumber(String membershipNumber) {
        this.membershipNumber = membershipNumber;
        if (this.studentId == null || this.studentId.isBlank()) {
            this.studentId = membershipNumber;
        }
    }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDepartment() {
        return department != null && !department.isBlank() ? department : departmentName;
    }
    public void setDepartment(String department) {
        this.department = department;
        if (this.departmentName == null || this.departmentName.isBlank()) {
            this.departmentName = department;
        }
    }

    public String getDepartmentName() {
        return departmentName != null && !departmentName.isBlank() ? departmentName : department;
    }
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
        if (this.department == null || this.department.isBlank()) {
            this.department = departmentName;
        }
    }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
}

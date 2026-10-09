package com.librax.model;

import java.time.LocalDate;

public class Member {
    private Integer id;
    private Integer userId;
    private String membershipNumber;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private Integer departmentId;
    private String departmentName;
    private Integer courseId;
    private String courseName;
    private LocalDate registrationDate;
    private LocalDate membershipExpiry;
    private MemberStatus status;

    public Member() {}

    public Member(Integer id, String membershipNumber, String fullName, String email, String phone,
                  String address, LocalDate registrationDate, LocalDate membershipExpiry, MemberStatus status) {
        this.id = id;
        this.membershipNumber = membershipNumber;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.registrationDate = registrationDate;
        this.membershipExpiry = membershipExpiry;
        this.status = status;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getMembershipNumber() { return membershipNumber; }
    public void setMembershipNumber(String membershipNumber) { this.membershipNumber = membershipNumber; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getName() { return fullName; }
    public void setName(String name) { this.fullName = name; }

    public String getMemberId() { return membershipNumber; }
    public void setMemberId(String memberId) { this.membershipNumber = memberId; }

    private int maxBooksAllowed = 5;
    private int currentBorrowedCount = 0;
    private double outstandingFines = 0.0;

    public int getMaxBooksAllowed() { return maxBooksAllowed; }
    public void setMaxBooksAllowed(int maxBooksAllowed) { this.maxBooksAllowed = maxBooksAllowed; }

    public int getCurrentBorrowedCount() { return currentBorrowedCount; }
    public void setCurrentBorrowedCount(int currentBorrowedCount) { this.currentBorrowedCount = currentBorrowedCount; }

    public double getOutstandingFines() { return outstandingFines; }
    public void setOutstandingFines(double outstandingFines) { this.outstandingFines = outstandingFines; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public LocalDate getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDate registrationDate) { this.registrationDate = registrationDate; }

    public LocalDate getMembershipExpiry() { return membershipExpiry; }
    public void setMembershipExpiry(LocalDate membershipExpiry) { this.membershipExpiry = membershipExpiry; }

    public MemberStatus getStatus() { return status; }
    public void setStatus(MemberStatus status) { this.status = status; }

    public String getDepartment() { return departmentName; }
    public void setDepartment(String department) { this.departmentName = department; }

    public String getCourse() { return courseName; }
    public void setCourse(String course) { this.courseName = course; }

    private String initialPassword;
    public String getInitialPassword() { return initialPassword; }
    public void setInitialPassword(String initialPassword) { this.initialPassword = initialPassword; }

    public LocalDate getRegisteredDate() { return registrationDate; }
    public void setRegisteredDate(LocalDate registeredDate) { this.registrationDate = registeredDate; }

    public LocalDate getExpiryDate() { return membershipExpiry; }
    public void setExpiryDate(LocalDate expiryDate) { this.membershipExpiry = expiryDate; }
}

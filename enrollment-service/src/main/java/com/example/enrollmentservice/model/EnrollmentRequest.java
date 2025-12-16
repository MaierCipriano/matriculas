package com.example.enrollmentservice.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class EnrollmentRequest {
    @NotBlank
    private String studentName;
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String courseName;

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
}
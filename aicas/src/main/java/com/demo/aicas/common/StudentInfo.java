package com.demo.aicas.common;

public class StudentInfo {
    private String courseId;
    private String courseName;
    private String studentId;
    private String studentName;
    private String mail;
    private String phone;

    public StudentInfo(String courseId, String courseName, String studentId, String studentName, String mail, String phone) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.studentId = studentId;
        this.studentName = studentName;
        this.mail = mail;
        this.phone = phone;
    }

    public String getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getMail() {
        return mail;
    }

    public String getPhone() {
        return phone;
    }
}

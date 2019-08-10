package com.demo.aicas.common;

public class TeacherInfo {
    private String courseName;
    private String teacherName;
    private String mail;
    private String phone;

    public TeacherInfo(String courseName, String teacherName, String mail, String phone) {
        this.courseName = courseName;
        this.teacherName = teacherName;
        this.mail = mail;
        this.phone = phone;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public String getMail() {
        return mail;
    }

    public String getPhone() {
        return phone;
    }
}

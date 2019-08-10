package com.demo.aicas.common;

import java.util.ArrayList;

public class SelectTeacher {
    private ArrayList<TeacherAttend> attendList;//签到表
    private ArrayList<StudentInfo> studentInfo;//学生信息表
    private String name;
    private String id;
    private String mail;
    private String phone;

    public SelectTeacher(ArrayList<TeacherAttend> attendList, ArrayList<StudentInfo> studentInfo, String name, String id, String mail, String phone) {
        this.attendList = attendList;
        this.studentInfo = studentInfo;
        this.name = name;
        this.id = id;
        this.mail = mail;
        this.phone = phone;
    }

    public ArrayList<TeacherAttend> getAttendList() {
        return attendList;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getMail() {
        return mail;
    }

    public String getPhone() {
        return phone;
    }

    public ArrayList<StudentInfo> getStudentInfo() {
        return studentInfo;
    }
}

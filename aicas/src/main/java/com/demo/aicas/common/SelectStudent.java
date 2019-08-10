package com.demo.aicas.common;

import java.util.ArrayList;

public class SelectStudent {
    private ArrayList<StudentAttend> attendList;//签到表
    private ArrayList<TeacherInfo> teacherInfo;//老师信息表
    private String name;
    private String id;
    private String classId;
    private String mail;
    private String phone;

    public SelectStudent(ArrayList<StudentAttend> attendList, ArrayList<TeacherInfo> teacherInfo, String name, String id, String classId, String mail, String phone) {
        this.attendList = attendList;
        this.teacherInfo = teacherInfo;
        this.name = name;
        this.id = id;
        this.classId = classId;
        this.mail = mail;
        this.phone = phone;
    }

    public ArrayList<StudentAttend> getAttendList() {
        return attendList;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getClassId() {
        return classId;
    }

    public String getMail() {
        return mail;
    }

    public String getPhone() {
        return phone;
    }

    public ArrayList<TeacherInfo> getTeacherInfo() {
        return teacherInfo;
    }
}

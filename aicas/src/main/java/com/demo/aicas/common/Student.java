package com.demo.aicas.common;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Student {
    private final String studentId;
    private final String studentName;
    @JsonIgnore
    private final String picturePath;

    public Student(String student_id, String studentName, String picture_path) {
        this.studentId = student_id;
        this.studentName = studentName;
        this.picturePath = picture_path;
    }


    public String getStudentId() {
        return this.studentId;
    }

    public String getPicturePath() {
        return this.picturePath;
    }

    public String getStudentName() {
        return studentName;
    }
}
